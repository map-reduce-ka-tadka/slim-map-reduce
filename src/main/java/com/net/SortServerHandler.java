package com.net;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.aws.AWSManager;
import com.main.ConfigParams;
import com.sort.SampleSort;
import com.utils.MessageHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * This Class handles all the requests from the Client
 * @author Abhijeet Sharma
 * @version 2.0
 * @since April 8, 2016 
 */
public class SortServerHandler extends ChannelInboundHandlerAdapter{

	static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	static int nInstances = ConfigParams.N_INSTANCES;
	static final int WORLD_STATE_START = 0;
	static Integer worldState = WORLD_STATE_START;    
	static Map<Integer, HashSet<String>> stateMap = new HashMap<Integer, HashSet<String>>();
	static Queue<String> filenameQueue;   
	static int shutDownCount = 0;
	static TreeSet<String> sampleSet = new TreeSet<String>();;
	static String pivots;
	static Map<String, Integer> addressMap = new HashMap<String, Integer>();
	static int counter = 0;
	static final int SUCCESS_STATUS = 1;
	static final int WAIT_STATUS = 0;
	static final int FAILURE_STATUS = -1;

	static final int CONNECTION_OPCODE = 0;
	static final int READ_AND_SAMPLE_DATA_OPCODE = 1;
	static final int FETCH_PIVOTS_OPCODE = 2;
	static final int PARTITION_AND_UPLOAD_DATA_OPCODE = 3;
	static final int MERGE_PARTITION_OPCODE = 4;
	static final int CLIENT_EXIT_OPCODE = -100;

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		SortServer.LOG.info("[START] A New Client has Connected with Address: {}", ctx.channel().remoteAddress());
		addressMap.put(ctx.channel().remoteAddress().toString(), SortServerHandler.counter);
		SortServerHandler.counter += 1;        
		if (addressMap.size() == nInstances){
			SortServer.LOG.info("All Clients Connected. Address Map: {}", addressMap.entrySet());
			worldState += 1;
		}
		channels.add(ctx.channel());
		super.handlerAdded(ctx);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		SortServer.LOG.info("[END] The Client has been disconnected with Address: {}", ctx.channel().remoteAddress());
		channels.remove(ctx.channel());
		super.handlerRemoved(ctx);
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws UnknownHostException {
		// TODO: Use ctx.channel().isWritable() then write to prevent OutOfMemoryError
		// See if everyone's ready
		SortServer.LOG.info("Connected to the client: {}", ctx.channel().remoteAddress());
		//System.out.println("Connected to the client" + ctx.channel().remoteAddress());    	
		ctx.writeAndFlush(new MessageHandler(CONNECTION_OPCODE, "Server Connected at: " + InetAddress.getLocalHost().getHostAddress(), SUCCESS_STATUS));
		/*Channel channel = ...;
		FileChannel fc = ...;
		channel.writeAndFlush(new DefaultFileRegion(fc, 0, fileLength));
		This only works if you not need to modify the data on the fly. If so use ChunkedWriteHandler and NioChunkedFile.*/

		/*Schedule and execute tasks via EventLoop this reduces the needed Threads and also makes sure it’s Thread-safe*/
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		//SortServer.LOG.info("Reading Channel from: {}", ctx.channel().remoteAddress());
		MessageHandler message = (MessageHandler) msg;
		SortServer.LOG.info("In Server, Received message: {}", message.toString());
		int clientCompletionCode = message.getCode();
		String clientMessage = message.getMessage();		
		int clientStatus = message.getStatus();

		if (clientStatus == FAILURE_STATUS){
			ctx.close();
		}
		else{
			if (worldState == WORLD_STATE_START){
				SortServer.LOG.info("Not all clients have connected.Please Wait..");			 
				ctx.writeAndFlush(new MessageHandler(clientCompletionCode, clientMessage, WAIT_STATUS));
			}
			else if (clientCompletionCode == (worldState - 1)){
				//add set
				//SortServer.LOG.info("Inside add set for Client: {}", ctx.channel().remoteAddress());
				if (clientCompletionCode == READ_AND_SAMPLE_DATA_OPCODE){
					//Store all samples in a set
					sampleSet.add(clientMessage); 
				}    			
				if (stateMap.containsKey(clientCompletionCode)){
					// Atleast one another client has also finished this step.
					// Add this step and Client IP to stateMap.
					HashSet<String> hs = stateMap.get(clientCompletionCode);
					hs.add(ctx.channel().remoteAddress().toString());
					stateMap.put(clientCompletionCode, hs);
					if (hs.size() == nInstances){
						// All clients have completed this step.
						// Increment World State and Proceed with next step
						if (clientCompletionCode == READ_AND_SAMPLE_DATA_OPCODE) {
							// Samples from all clients received, Find pivots.
							String samples = StringUtils.join(sampleSet, ",");
							pivots = SampleSort.fetchPivotsFromSamples(samples);
							worldState += 1;
						}
						worldState += 1;
						SortServer.LOG.info("Client {} has changed World State. Client Message: {} , new WorldState: {}", 
								ctx.channel().remoteAddress(), msg, worldState.toString());
						executeNextStep(ctx, clientCompletionCode, clientMessage);
					}  
					else{
						// Not all Clients have finished this Step.Wait.
						ctx.writeAndFlush(new MessageHandler(clientCompletionCode, clientMessage, WAIT_STATUS));
					}
				}
				else{
					// This is the first client to finish this step.
					// Add this step and Client IP to stateMap and wait.
					HashSet<String> hs = new HashSet<String>();
					hs.add(ctx.channel().remoteAddress().toString());
					stateMap.put(clientCompletionCode, hs);
					ctx.writeAndFlush(new MessageHandler(clientCompletionCode, clientMessage, WAIT_STATUS));
				}  
			}
			else if ((clientCompletionCode < (worldState - 1)) || (clientCompletionCode == CLIENT_EXIT_OPCODE)){
				//Free pass for clients who have lagged behind
				//SortServer.LOG.info("Inside free pass for Client: {}", ctx.channel().remoteAddress()) ;
				executeNextStep(ctx, clientCompletionCode, clientMessage);
			}
		}
	}

	public void executeNextStep(ChannelHandlerContext ctx, int code, String message){
		MessageHandler result = null;

		switch (code) {
		case CONNECTION_OPCODE:
			SortServer.LOG.info("Code: {}, Sending Address Map to client: {}", code, addressMap.entrySet().toString());
			//code += 1;			 			
			result = new MessageHandler(READ_AND_SAMPLE_DATA_OPCODE, addressMap.entrySet() + "ID" + addressMap.get(ctx.channel().remoteAddress().toString()), SUCCESS_STATUS);
			//SortServer.LOG.info("sending.. {}", result);
			ctx.writeAndFlush(result);
			break;
		case READ_AND_SAMPLE_DATA_OPCODE: 
			SortServer.LOG.info("Code: {}, Sending the client pivots: {}", code, pivots);	
			result = new MessageHandler(PARTITION_AND_UPLOAD_DATA_OPCODE, pivots, SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;
		case PARTITION_AND_UPLOAD_DATA_OPCODE: 
			SortServer.LOG.info("Code: {}, Client is requesting to Merge partitions", code);
			result = new MessageHandler(MERGE_PARTITION_OPCODE, "Merge Partitions", SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;
		case CLIENT_EXIT_OPCODE:
			SortServer.LOG.info("Code: {}, Client is requesting shutdown", code);
			SortServer.LOG.info("Closing client: {}", ctx.channel().remoteAddress());
			ctx.close();
			shutDownCount += 1;
			if (shutDownCount == nInstances){
				// All clients should have exited					
				try {
					FileWriter fw = new FileWriter("_SUCCESS", false);
					fw.close();
					new AWSManager().sendFileToS3("_SUCCESS", ConfigParams.OUTPUT_FOLDER + "/_SUCCESS");						
				} 
				catch (IOException e) {
					e.printStackTrace();
				}					
				ctx.channel().parent().close();
			}
			break;
		default: 
			SortServer.LOG.warn("Code: {}, Something's wrong. Client cannot send this code. Message: {}", code, message);
			ctx.writeAndFlush(new MessageHandler(code, message, FAILURE_STATUS));
			break;
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
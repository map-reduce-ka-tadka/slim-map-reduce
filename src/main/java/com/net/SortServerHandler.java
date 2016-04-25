package com.net;

import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.aws.AWSManager;
import com.main.ServerMain;
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

	static final int WORLD_STATE_START = 0;
	// Message Status
	static final int SUCCESS_STATUS = 1;
	static final int WAIT_STATUS = 0;
	static final int FAILURE_STATUS = -1;
	// Message OpCodes
	static final int CLIENT_HANDSHAKE_OPCODE = 0;
	static final int MAP_OPCODE = 1;
	static final int SORT_READ_AND_SAMPLE_DATA_OPCODE = 2;
	static final int SORT_FETCH_PIVOTS_OPCODE = 3;
	static final int SORT_PARTITION_AND_UPLOAD_DATA_OPCODE = 4;
	static final int SORT_MERGE_PARTITION_OPCODE = 5;
	static final int REDUCE_OPCODE = 6;
	static final int CLIENT_EXIT_OPCODE = -100;
	// Client Num Counter
	static int counter = 0;
	static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	static HashSet<String> connectionSet = new HashSet<String>();
	static Integer worldState = WORLD_STATE_START;    
	static Map<Integer, HashSet<String>> stateMap = new HashMap<Integer, HashSet<String>>();
	static Queue<String> filenameQueue;   
	static int shutDownCount = 0;
	static ArrayList<String> sampleList = new ArrayList<String>();
	static String pivots;
	static Map<String, String> addressMap = new HashMap<String, String>();

	/**
	 * Method for behavior when a new channel handler is added
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		SortServer.LOG.info("[START] A New Client has Connected with Address: {}", ctx.channel().remoteAddress());
		ServerMain.ACTIVE_INSTANCES += 1;
		channels.add(ctx.channel());
		super.handlerAdded(ctx);
	}

	/**
	 * Method for behavior when a new channel handler is removed
	 */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		SortServer.LOG.info("[END] The Client has been disconnected with Address: {}", ctx.channel().remoteAddress());
		ServerMain.ACTIVE_INSTANCES -= 1;
		channels.remove(ctx.channel());
		super.handlerRemoved(ctx);
	}

	/**
	 * Method for behavior when a channel is active.
	 */
	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws UnknownHostException {
		SortServer.LOG.info("Connected to the client: {}", ctx.channel().remoteAddress());
		connectionSet.add(ctx.channel().remoteAddress().toString());       
		// See if all clients are ready to move to next step
		if (connectionSet.size() == ServerMain.N_INSTANCES){
			SortServer.LOG.info("All Clients Connected. connectionSet: {}", connectionSet.toString());
			worldState += 1;
		}   	
		ctx.writeAndFlush(new MessageHandler(CLIENT_HANDSHAKE_OPCODE, ServerMain.JOB_ID + "_" + ServerMain.N_INSTANCES, SUCCESS_STATUS));
	}

	/**
	 * Method for behavior when a channel is read.
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		SortServer.LOG.info("Reading Channel from: {}", ctx.channel().remoteAddress());
		MessageHandler message = (MessageHandler) msg;
		SortServer.LOG.info("In Server, Received message: {}, WorldState: {}", message.toString(), worldState);
		int clientCompletionCode = message.getCode();
		String clientMessage = message.getMessage();		
		int clientStatus = message.getStatus();
		if (clientStatus == FAILURE_STATUS){
			SortServer.LOG.info("Failure from Client.Shutting Down Client and Creating Error File.");
			try {
				FileWriter fw = new FileWriter("_ERROR", false);
				fw.close();
				new AWSManager().sendFileToS3("_ERROR", ServerMain.OUTPUT_FOLDER + "/_ERROR");
				ctx.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}			
		}
		else{
			if (worldState == WORLD_STATE_START){
				SortServer.LOG.info("Not all clients have connected.Please Wait..");			 
				ctx.writeAndFlush(new MessageHandler(clientCompletionCode, clientMessage, WAIT_STATUS));
			}
			else if (clientCompletionCode == (worldState - 1)){
				if (clientCompletionCode == CLIENT_HANDSHAKE_OPCODE){
					SortClient.LOG.info("ClientId & input/output params received from Client: {}", message.toString());
					String[] clientMessageArr = clientMessage.split("_");
					String clientId = clientMessageArr[0];
					ServerMain.INPUT_PATH = clientMessageArr[1];
					ServerMain.OUTPUT_PATH = clientMessageArr[2];
					String[] outputPathSplit = ServerMain.OUTPUT_PATH.split("/");
					ServerMain.OUTPUT_BUCKET = outputPathSplit[2];
					ServerMain.OUTPUT_FOLDER = StringUtils.join(Arrays.asList(outputPathSplit).subList(3, outputPathSplit.length), "/");
					if (!addressMap.containsKey(clientMessage)){
						addressMap.put(clientId, counter + "\t" + ctx.channel().remoteAddress().toString());
						counter += 1;
					}
				}
				else if (clientCompletionCode == SORT_READ_AND_SAMPLE_DATA_OPCODE){
					//Store all samples in a set
					sampleList.add(clientMessage);
					Collections.sort(sampleList, String.CASE_INSENSITIVE_ORDER);
				}
				if (stateMap.containsKey(clientCompletionCode)){
					// Atleast one another client has also finished this step.
					// Add this step and Client IP to stateMap.
					HashSet<String> hs = stateMap.get(clientCompletionCode);
					hs.add(ctx.channel().remoteAddress().toString());
					stateMap.put(clientCompletionCode, hs);
					if (hs.size() == ServerMain.N_INSTANCES){
						// All clients have completed this step.
						// Increment World State and Proceed with next step
						if (clientCompletionCode == SORT_READ_AND_SAMPLE_DATA_OPCODE) {
							// Samples from all clients received, Find pivots.
							String samples = StringUtils.join(sampleList, ",");
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
				executeNextStep(ctx, clientCompletionCode, clientMessage);
			}
		}
	}

	/**
	 * Method to execute next step.
	 * @param ctx
	 * @param code
	 * @param message
	 */
	public void executeNextStep(ChannelHandlerContext ctx, int code, String message){
		MessageHandler result = null;
		switch (code) {
		case CLIENT_HANDSHAKE_OPCODE:
			SortServer.LOG.info("Code: {}, Sending Address Map to client: {}", code, addressMap.entrySet().toString()); 			
			result = new MessageHandler(MAP_OPCODE, addressMap.entrySet().toString(), SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;
		case MAP_OPCODE:
			SortServer.LOG.info("Map Complete. Code: {}, Message: {}", code, message); 			
			result = new MessageHandler(SORT_READ_AND_SAMPLE_DATA_OPCODE, "Start Sort", SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;
		case SORT_READ_AND_SAMPLE_DATA_OPCODE: 
			SortServer.LOG.info("Code: {}, Sending the client pivots: {}", code, pivots);	
			result = new MessageHandler(SORT_PARTITION_AND_UPLOAD_DATA_OPCODE, pivots, SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;
		case SORT_PARTITION_AND_UPLOAD_DATA_OPCODE: 
			SortServer.LOG.info("Code: {}, Client is requesting to Merge partitions", code);
			result = new MessageHandler(SORT_MERGE_PARTITION_OPCODE, "Merge Partitions", SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;
		case SORT_MERGE_PARTITION_OPCODE: 
			SortServer.LOG.info("Code: {}, Shuffle-Sort Complete, Message: {}", code, message);
			result = new MessageHandler(REDUCE_OPCODE, "Start Reduce", SUCCESS_STATUS);
			ctx.writeAndFlush(result);
			break;			
		case CLIENT_EXIT_OPCODE:
			SortServer.LOG.info("Code: {}, Client is requesting shutdown", code);
			SortServer.LOG.info("Closing client: {}", ctx.channel().remoteAddress());
			ctx.close();
			shutDownCount += 1;
			if (shutDownCount == ServerMain.N_INSTANCES){
				// All clients should have exited					
				try {
					FileWriter fw = new FileWriter("_SUCCESS", false);
					fw.close();
					new AWSManager().sendFileToS3("_SUCCESS", ServerMain.OUTPUT_FOLDER + "/_SUCCESS");
					new AWSManager().sendFileToS3(ServerMain.LOGS_PATH + "/" + "server_"+ ServerMain.JOB_ID + ".log", 
							ServerMain.LOGS_PATH + "/" + "server_"+ ServerMain.JOB_ID + ".log");
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

	/**
	 * Method for behavior when a channel read is complete.
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	/**
	 * Method for behavior when a exception occurs in a channel.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		try {
			FileWriter fw = new FileWriter("_ERROR", false);
			fw.close();
			new AWSManager().sendFileToS3("_ERROR", ServerMain.OUTPUT_FOLDER + "/_ERROR");
			ctx.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
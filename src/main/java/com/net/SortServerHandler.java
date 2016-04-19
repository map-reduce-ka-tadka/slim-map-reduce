/*
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */

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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class SortServerHandler extends ChannelInboundHandlerAdapter{
	
	static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	static int nInstances = ConfigParams.N_INSTANCES;
	static Integer worldState = 0;    
	static Map<String, HashSet<String>> stateMap = new HashMap<String, HashSet<String>>();
	static Queue<String> filenameQueue;   
	static int shutDownCount = 0;
	static TreeSet<String> sampleSet = new TreeSet<String>();;
	static String pivots;
	static Map<String, Integer> addressMap = new HashMap<String, Integer>();
	static int counter = 0;

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
		ctx.writeAndFlush("0_Server Connected at: " + InetAddress.getLocalHost().getHostAddress());
		/*Channel channel = ...;
		FileChannel fc = ...;
		channel.writeAndFlush(new DefaultFileRegion(fc, 0, fileLength));
		This only works if you not need to modify the data on the fly. If so use ChunkedWriteHandler and NioChunkedFile.*/
		
		/*Schedule and execute tasks via EventLoop this reduces the needed Threads and also makes sure it’s Thread-safe*/
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		//SortServer.LOG.info("Reading Channel from: {}", ctx.channel().remoteAddress());
		SortServer.LOG.info("In Server, Received message: {}", msg.toString());
		String[] clientMessage = ((String) msg).split("_");
		Integer clientCode = Integer.parseInt(clientMessage[0]);
		if (worldState == 0){
			// do nothing
			//SortServer.LOG.info("Not all clients have connected.Please Wait..");
			ctx.writeAndFlush("Wait" + "_"  + msg);
		}
		else if (clientCode == (worldState - 1)){
			//add set
			//SortServer.LOG.info("Inside add set for Client: {}", ctx.channel().remoteAddress());
			if (clientCode == 1){
				//Store all samples in a set
				sampleSet.add(clientMessage[1]); 
			}    			
			if (stateMap.containsKey(clientMessage[0])){
				HashSet<String> hs = stateMap.get(clientMessage[0]);
				hs.add(ctx.channel().remoteAddress().toString());
				stateMap.put(clientMessage[0], hs);
				if (hs.size() == nInstances){
					if (clientCode == 1) {
						String samples = StringUtils.join(sampleSet, ",");
						pivots = SampleSort.phaseTwo(samples);
					}
					worldState += 1;
					SortServer.LOG.info("Client {} has changed World State. Client Message: {} , new WorldState: {}", 
							ctx.channel().remoteAddress(), msg, worldState.toString());
					doFunction(ctx, clientMessage[1], clientCode);
				}  
				else{
					ctx.writeAndFlush("Wait" + "_"  + msg);
				}
			}
			else{
				HashSet<String> hs = new HashSet<String>();
				hs.add(ctx.channel().remoteAddress().toString());
				stateMap.put(clientMessage[0], hs);
				ctx.writeAndFlush("Wait" + "_"  + msg);
			}  
		}
		else if ((clientCode < (worldState - 1)) || (clientCode == -100)){
			//free pass for clients who have lagged behind
			//SortServer.LOG.info("Inside free pass for Client: {}", ctx.channel().remoteAddress()) ;
			doFunction(ctx, clientMessage[1], clientCode);
		}   	
	}

	public void doFunction(ChannelHandlerContext ctx, String msg, Integer code){
		String result = "";
		code += 1;
		switch (code) {
		case 1:	SortServer.LOG.info("Code: {}, Sending Address Map to client: {}", code-1, addressMap.entrySet().toString());; 			
				result = code.toString() + "_" + addressMap.entrySet() + "ID" + addressMap.get(ctx.channel().remoteAddress().toString());
				//SortServer.LOG.info("sending.. {}", result);
				ctx.writeAndFlush(result);
				break;
		case 2: SortServer.LOG.info("Code: {}, Sending the client pivots: {}", code-1, pivots);	
				result = code.toString() + "_" + pivots;
				ctx.writeAndFlush(result);
				break;
		case 3: SortServer.LOG.info("Code: {}, Client is requesting to Merge partitions", code-1);
				result = code.toString() + "_" + "Merge Partitions";
				ctx.writeAndFlush(result);
				break;
		case -99:
				SortServer.LOG.info("Code: {}, Client is requesting shutdown", code-1);
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
			SortServer.LOG.warn("Code: {}, Something's wrong. Client cannot send this code. Message: {}", code-1, msg);
			ctx.writeAndFlush("Wait" + "_"  + code + "_" + msg);
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
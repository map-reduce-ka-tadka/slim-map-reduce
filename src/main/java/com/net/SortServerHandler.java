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
	public SortServerHandler(){
		System.out.println("Server started and waiting for clients to connect.");
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[START] A New Client has Connected with Address : " + ctx.channel().remoteAddress());
		addressMap.put(ctx.channel().remoteAddress().toString(), SortServerHandler.counter);
		SortServerHandler.counter += 1;        
		if (addressMap.size() == nInstances){
			worldState += 1;
		}
		channels.add(ctx.channel());
		super.handlerAdded(ctx);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[END] The Client has been disconnected with Address : " + ctx.channel().remoteAddress());
		channels.remove(ctx.channel());
		super.handlerRemoved(ctx);
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws UnknownHostException {
		// See if everyone's ready
		System.out.println("Connected to the client" + ctx.channel().remoteAddress());    	
		ctx.writeAndFlush("0_Hi, You are Connected to Server now in: " + InetAddress.getLocalHost().getHostAddress());

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		System.out.println("addressList: " + addressMap.entrySet());
		System.out.println("Reading Channel from " + ctx.channel().remoteAddress());
		System.out.println("In Server, Received message: " + (String) msg);
		String[] clientMessage = ((String) msg).split("_");
		Integer clientCode = Integer.parseInt(clientMessage[0]);
		if (worldState == 0){
			// do nothing
			System.out.println("Not all clients have connected.Please Wait..");
			ctx.writeAndFlush("Sabad rakh" + "_"  + msg);
		}
		else if (clientCode == (worldState - 1)){
			//add set
			System.out.println("Inside add set for Client: " + ctx.channel().remoteAddress()) ;
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
					System.out.println("Client " + ctx.channel().remoteAddress() + 
							" has changed World State. Client Message: " + msg + 
							" , new WorldState: " + worldState.toString());
					doFunction(ctx, clientMessage[1], clientCode);
				}  
				else{
					ctx.writeAndFlush("Sabad rakh" + "_"  + msg);
				}
			}
			else{
				HashSet<String> hs = new HashSet<String>();
				hs.add(ctx.channel().remoteAddress().toString());
				stateMap.put(clientMessage[0], hs);
				ctx.writeAndFlush("Sabad rakh" + "_"  + msg);
			}  
		}
		else if ((clientCode < (worldState - 1)) || (clientCode == -100)){
			//free pass for clients who have lagged behind
			System.out.println("Inside free pass for Client: " + ctx.channel().remoteAddress()) ;
			doFunction(ctx, clientMessage[1], clientCode);
		}   	
	}

	public void doFunction(ChannelHandlerContext ctx, String msg, Integer code){
		String result = "";
		code += 1;
		switch (code) {
		case 1: System.out.println("Sending files to client: " + addressMap.entrySet().toString());				
		result = code.toString() + "_" + addressMap.entrySet() + "ID" + addressMap.get(ctx.channel().remoteAddress().toString());
		System.out.println("sending.."+result);
		ctx.writeAndFlush(result);
		break;
		case 2: System.out.println("Sending Client pivots: " + pivots);		
		//String pivotValues = "5, 21";
		result = code.toString() + "_" + pivots;
		ctx.writeAndFlush(result);
		break;
		case 3: System.out.println("Client is asking for Green Signal to Merge");	
		result = code.toString() + "_" + "Jaa, Jee le apni Zindagi";
		ctx.writeAndFlush(result);
		break;
		case -99:	System.out.println("Client is asking for Shutdown");
		System.out.println("Closing client: " + ctx.channel().remoteAddress());
		ctx.close();
		shutDownCount += 1;
		if (shutDownCount == nInstances){
			// All clients should have exited
			System.out.println("Server exit");
			try {
				FileWriter fw = new FileWriter("finish",false);
				fw.close();
				new AWSManager().sendFileToS3("finish", ConfigParams.OUTPUT_FOLDER+"/_SUCCESS");
			} catch (IOException e) {
				e.printStackTrace();
			}
			ctx.channel().parent().close();
		}
		break;
		default: System.out.println("Something's wrong. Client cannot send " + code + " code. Message: " + msg);
		ctx.writeAndFlush("Sabad rakh" + "_"  + code + "_" + msg);
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
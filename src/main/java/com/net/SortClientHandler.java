/*
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.sort.SampleSort;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SortClientHandler extends ChannelInboundHandlerAdapter{
	public SampleSort ss;
	public static HashMap<String, Integer> addMap;
	public static int clientId;

	public SortClientHandler() {
		ss = new SampleSort();
		addMap = new HashMap<String, Integer>();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws UnknownHostException {
		System.out.println("[" + InetAddress.getLocalHost().getHostAddress().toString() + "] " + " is Connected to Server: " + ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException {
		//SortClient.LOG.info("Received message: {}", msg.toString());
		String[] message = msg.toString().split("_");
		String request = "";
		if (message[0].equalsIgnoreCase("0")){
			SortClient.LOG.info("Code: {}, Message received: {}", message[0], message[1]);
			Thread.sleep(6000);
			SortClient.LOG.info("Requesting Address Map from Server.. ");
			request = "0_Requesting Files";
		}
		else if (message[0].equalsIgnoreCase("1")){
			SortClient.LOG.info("Received clientId from Server: {}", message[1]);
			String[] add_count = message[1].split("ID");
			clientId = Integer.parseInt(add_count[1]);
			String[] addList = add_count[0].replace("[", "").replace("]", "").replace("/", "").split(",");
			for (String elem : addList){
				String[] elemList = elem.split("=");
				addMap.put(elemList[0].split(":")[0],Integer.parseInt(elemList[1]));
			}
			SortClient.LOG.info("[START PHASE 1] => Read, Sort and Sample");
			String clientSamples = ss.phaseOne(clientId);
			SortClient.LOG.info("[END PHASE 1]  => Read, Sort and Sample");
			SortClient.LOG.info("Requesting Global Pivots from Server");
			request = "1_" + clientSamples;
		}
		else if (message[0].equalsIgnoreCase("2")){
			SortClient.LOG.info("Pivots received from Server: {}", message[1]);
			SortClient.LOG.info("[START PHASE 3] => Partition and Exchange Data");
			ss.phaseThree(message[1], clientId);
			SortClient.LOG.info("[END PHASE 3] => Partition and Exchange Data");
			SortClient.LOG.info("Requesting Server for Merging Partitions");
			request = "2_" + "Request Merge"; 
		}
		else if (message[0].equalsIgnoreCase("3")){
			SortClient.LOG.info("Pivots received from Server: {}", message[1]);
			SortClient.LOG.info("Code: {}, Message received: {}", message[0], message[1]);
			SortClient.LOG.info("[START PHASE 3] => Merge Data");
			ss.phaseFour(clientId);
			SortClient.LOG.info("[END PHASE 3] => Merge Data");
			SortClient.LOG.info("Requesting server to shutdown Client");
			request = "-100_Shutdown CLient: " + InetAddress.getLocalHost().getHostAddress().toString();
		}
		else{
			SortClient.LOG.info("Code: {}, Message received: {}", message[0], message[1]);
			request = StringUtils.join(Arrays.copyOfRange(message, 1, message.length), "_");   
			Thread.sleep(6000);
			System.out.println("Requesting server again..");
		}
		ctx.write(request);
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
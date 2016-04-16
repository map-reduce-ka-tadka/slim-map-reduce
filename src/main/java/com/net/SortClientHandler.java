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
		System.out.println("In client, Received message: " + (String) msg);
		String[] message = ((String) msg).split("_");
		System.out.println("code: " + message[0]);
		String request = "";
		if (message[0].equalsIgnoreCase("0")){
			Thread.sleep(6000);
			System.out.println("Requesting Filenames from Server.. ");
			request = "0_GimmeFiles!";
		}
		else if (message[0].equalsIgnoreCase("1")){
			System.out.println("Received clientId from Server: " + message[1]);
			String[] add_count = message[1].split("ID");
			clientId = Integer.parseInt(add_count[1]);
			String[] addList = add_count[0].replace("[", "").replace("]", "").replace("/", "").split(",");
			for (String elem : addList){
				String[] elemList = elem.split("=");
				addMap.put(elemList[0].split(":")[0],Integer.parseInt(elemList[1]));
			}
			System.out.println("[START PHASE 1] => Read, Sort and Sample");
			String clientSamples = ss.phaseOne(clientId);
			System.out.println("[END PHASE 1]  => Read, Sort and Sample");
			System.out.println("Fetching Global Pivots from Server");

			request = "1_" + clientSamples;
		}
		else if (message[0].equalsIgnoreCase("2")){
			System.out.println("Client Id is: " + clientId);
			System.out.println("Pivots received from Server: " + message);
			System.out.println("[START PHASE 3] => Partition and Exchange Data");
			ss.phaseThree(message[1], clientId);
			System.out.println("Exchanging Partitions..");
			System.out.println("[END PHASE 3] => Partition and Exchange Data");
			System.out.println("Asking Server for Green Signal for Merging");
			request = "2_" + "Signal de be!"; 
		}
		else if (message[0].equalsIgnoreCase("3")){
			System.out.println("Client Id is: " + clientId);
			System.out.println("Green Signal received from Server: " + message);
			System.out.println("[START PHASE 3] => Merge Data");
			ss.phaseFour(clientId);    		
			System.out.println("[END PHASE 3] => Merge Data");
			System.out.println("Shutting Down Client..");
			request = "-100_Shutdown CLient: " + InetAddress.getLocalHost().getHostAddress().toString();
		}
		else{
			System.out.println("Server is waiting for all clients to complete. Client Address: " + InetAddress.getLocalHost().getHostAddress());
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
/*
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import com.sort.SampleSort;
import com.utils.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SortClientHandler extends ChannelInboundHandlerAdapter{
	public SampleSort ss;
	public static HashMap<String, Integer> addMap;
	public static int clientId;
	static final int SUCCESS_STATUS = 1;
	static final int WAIT_STATUS = 0;
	static final int FAILURE_STATUS = -1;
	
	static final int CONNECTION_OPCODE = 0;
	static final int READ_AND_SAMPLE_DATA_OPCODE = 1;
	static final int FETCH_PIVOTS_OPCODE = 2;
	static final int PARTITION_AND_UPLOAD_DATA_OPCODE = 3;
	static final int MERGE_PARTITION_OPCODE = 4;
	static final int CLIENT_EXIT_OPCODE = -100;

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
		MessageHandler message = (MessageHandler) msg;
		SortClient.LOG.info("In Client, Received message: {}", message.toString());
		int requestCodeFromServer = message.getCode();
		String messageFromServer = message.getMessage();		
		int StatusFromServer = message.getStatus();

		MessageHandler response = null;
		
		if (StatusFromServer == FAILURE_STATUS){
			response = new MessageHandler(requestCodeFromServer, messageFromServer, FAILURE_STATUS);
		}
		else if (StatusFromServer == WAIT_STATUS){
			SortClient.LOG.info("Code: {}, Message received: {}", requestCodeFromServer, messageFromServer);			   
			Thread.sleep(6000);
			System.out.println("Requesting server again..");
			response =  new MessageHandler(requestCodeFromServer, messageFromServer, SUCCESS_STATUS);
		}
		else{
			if (requestCodeFromServer == CONNECTION_OPCODE){
				SortClient.LOG.info("Code: {}, Message received: {}", requestCodeFromServer, messageFromServer);
				Thread.sleep(6000);
				SortClient.LOG.info("Requesting Address Map from Server.. ");
				response = new MessageHandler(CONNECTION_OPCODE, "Requesting Address Map", SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == READ_AND_SAMPLE_DATA_OPCODE){
				SortClient.LOG.info("Received clientId from Server: {}", messageFromServer);
				String[] add_count = messageFromServer.split("ID");
				clientId = Integer.parseInt(add_count[1]);
				String[] addList = add_count[0].replace("[", "").replace("]", "").replace("/", "").split(",");
				for (String elem : addList){
					String[] elemList = elem.split("=");
					addMap.put(elemList[0].split(":")[0],Integer.parseInt(elemList[1]));
				}
				SortClient.LOG.info("[START PHASE 1] => Read, Sort and Sample");
				String clientSamples = ss.readAndSampleData(clientId);
				SortClient.LOG.info("[END PHASE 1]  => Read, Sort and Sample");
				SortClient.LOG.info("Requesting Global Pivots from Server");
				response = new MessageHandler(READ_AND_SAMPLE_DATA_OPCODE, clientSamples, SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == PARTITION_AND_UPLOAD_DATA_OPCODE){
				SortClient.LOG.info("Pivots received from Server: {}", messageFromServer);
				SortClient.LOG.info("[START PHASE 3] => Partition and Exchange Data");
				ss.partitionAndUploadData(messageFromServer, clientId);
				SortClient.LOG.info("[END PHASE 3] => Partition and Exchange Data");
				SortClient.LOG.info("Requesting Server for Merging Partitions");
				response = new MessageHandler(PARTITION_AND_UPLOAD_DATA_OPCODE, "Request Merge", SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == MERGE_PARTITION_OPCODE){
				SortClient.LOG.info("Code: {}, Message received: {}", requestCodeFromServer, messageFromServer);
				SortClient.LOG.info("[START PHASE 4] => Merge Data");
				ss.phaseFour(clientId);
				SortClient.LOG.info("[END PHASE 4] => Merge Data");
				SortClient.LOG.info("Requesting server to shutdown Client");
				response = new MessageHandler(CLIENT_EXIT_OPCODE, "Request Merge", SUCCESS_STATUS);
			}
		}
		ctx.write(response);
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
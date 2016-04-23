
package com.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.aws.AWSManager;
import com.main.ClientMain;
import com.map.Map;
import com.reduce.Reduce;
import com.sort.SampleSort;
import com.utils.FileUtils;
import com.utils.MessageHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */
public class SortClientHandler extends ChannelInboundHandlerAdapter{
	public SampleSort ss;
	public static HashMap<String, String> addressMap = new HashMap<String, String>();
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

	public SortClientHandler() {
		ss = new SampleSort();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws UnknownHostException {
		SortClient.LOG.info("[{}] is Connected to Server: {}", InetAddress.getLocalHost().getHostAddress().toString(), ctx.channel().remoteAddress());
	}

	/**
	 * Reads the message Handler object from the incoming Channel
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException, IllegalAccessException, InstantiationException {
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
			SortClient.LOG.info("Message received from Server: {}", message.toString());			   
			Thread.sleep(6000);
			SortClient.LOG.info("Requesting Server Again..");
			response =  new MessageHandler(requestCodeFromServer, messageFromServer, SUCCESS_STATUS);
		}
		else{
			if (requestCodeFromServer == CLIENT_HANDSHAKE_OPCODE){
				SortClient.LOG.info("Message received from Server: {}", message.toString());
				String[] handshakeMessage = messageFromServer.split("_");
				ClientMain.JOB_ID = handshakeMessage[0];
				FileUtils.createDir(ClientMain.JOB_ID);
				ClientMain.TEMP_PATH = ClientMain.JOB_ID + "/" + "_temp";
				FileUtils.createDir(ClientMain.TEMP_PATH);
				ClientMain.N_INSTANCES = Integer.parseInt(handshakeMessage[1]);
				Thread.sleep(6000);
				SortClient.LOG.info("Sending Client ID to Server.. ");
				response = new MessageHandler(CLIENT_HANDSHAKE_OPCODE, ClientMain.CLIENT_ID+"_"+ClientMain.INPUT_PATH+"_"+ClientMain.OUTPUT_PATH, SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == MAP_OPCODE){
				SortClient.LOG.info("Received AddressMap from Server: {}", messageFromServer);
				ClientMain.CURRENT_OPCODE = MAP_OPCODE;
				String[] addList = messageFromServer.replace("[", "").replace("]", "").replace("/", "").split(",");
				for (String elem : addList){
					String[] elemList = elem.split("=");
					if (elemList[0].trim().equals(ClientMain.CLIENT_ID)){
						ClientMain.CLIENT_NUM = Integer.parseInt(elemList[1].split("\t")[0]);
					}
					addressMap.put(elemList[0], elemList[1].split("\t")[1]);
				}
				SortClient.LOG.info("[START MAP PHASE] => Map");
				Map m = new Map();
				ClientMain.MAP_PATH = ClientMain.TEMP_PATH + "/" + "map";
				FileUtils.createDir(ClientMain.MAP_PATH);
				m.map(ClientMain.CLIENT_NUM);
				SortClient.LOG.info("[END MAP PHASE]  => Map");
				SortClient.LOG.info("Requesting to Start Shuffle-Sort Step");
				response = new MessageHandler(MAP_OPCODE, "Map Done", SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == SORT_READ_AND_SAMPLE_DATA_OPCODE){
				SortClient.LOG.info("Received Distributed Sort from Server: {}", messageFromServer);
				SortClient.LOG.info("[START SORT PHASE 1] => Read, Sort and Sample");
				ClientMain.SORT_PATH = ClientMain.TEMP_PATH + "/" + "sort";
				FileUtils.createDir(ClientMain.SORT_PATH);
				String clientSamples = ss.readAndSampleData(ClientMain.CLIENT_NUM);
				SortClient.LOG.info("[END SORT PHASE 1]  => Read, Sort and Sample");
				SortClient.LOG.info("Requesting Global Pivots from Server");
				response = new MessageHandler(SORT_READ_AND_SAMPLE_DATA_OPCODE, clientSamples, SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == SORT_PARTITION_AND_UPLOAD_DATA_OPCODE){
				SortClient.LOG.info("Pivots received from Server: {}", messageFromServer);
				SortClient.LOG.info("[START SORT PHASE 3] => Partition and Exchange Data");
				ss.partitionAndUploadData(messageFromServer, ClientMain.CLIENT_NUM);
				SortClient.LOG.info("[END SORT PHASE 3] => Partition and Exchange Data");
				SortClient.LOG.info("Requesting Server for Merging Partitions");
				response = new MessageHandler(SORT_PARTITION_AND_UPLOAD_DATA_OPCODE, "Request Merge", SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == SORT_MERGE_PARTITION_OPCODE){
				SortClient.LOG.info("Code: {}, Message received: {}", requestCodeFromServer, messageFromServer);
				SortClient.LOG.info("[START SORT PHASE 4] => Merge Data");
				ss.phaseFour(ClientMain.CLIENT_NUM);
				SortClient.LOG.info("[END SORT PHASE 4] => Merge Data");
				SortClient.LOG.info("Requesting to Start Reducer Step");
				SortClient.LOG.info("Requesting server to shutdown Client");
				response = new MessageHandler(SORT_MERGE_PARTITION_OPCODE, "Request Reduce", SUCCESS_STATUS);
			}
			else if (requestCodeFromServer == REDUCE_OPCODE){
				SortClient.LOG.info("Code: {}, Message received: {}", requestCodeFromServer, messageFromServer);
				ClientMain.CURRENT_OPCODE = REDUCE_OPCODE;
				SortClient.LOG.info("[START REDUCE PHASE] => Reduce");
				Reduce r = new Reduce();
				ClientMain.LOCAL_OUTPUT_PATH = ClientMain.OUTPUT_FOLDER;
				FileUtils.createDir(ClientMain.LOCAL_OUTPUT_PATH);
				r.reduce(ClientMain.CLIENT_NUM);
				SortClient.LOG.info("[END REDUCE PHASE] => Reduce");
				SortClient.LOG.info("Requesting server to shutdown Client");
				new AWSManager().sendFileToS3(ClientMain.LOGS_PATH + "/" + "client_"+ ClientMain.CLIENT_ID + ".log", 
						ClientMain.LOGS_PATH + "/" + "client_"+ ClientMain.CLIENT_ID + ".log");
				response = new MessageHandler(CLIENT_EXIT_OPCODE, "Request Shutdown", SUCCESS_STATUS);
			}
		}
		ctx.write(response);
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
		ctx.close();
	}
}
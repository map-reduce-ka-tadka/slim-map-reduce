
package com.main;

import java.util.UUID;

import com.net.SortServer;
import com.utils.FileUtils;
/**
 * Main Class for Server
 * @author Abhijeet Sharma, Deepen Mehta
 * @since Apr 10, 2016
 *
 */
public class ServerMain {
	// Initialized by Server
	public static String SERVER_ADDRESS;
	public static int SERVER_PORT;
	public static int N_INSTANCES;
	
	public static String INPUT_PATH;
	public static String OUTPUT_PATH;
	public static String OUTPUT_BUCKET;
	public static String OUTPUT_FOLDER;
	public static String JOB_ID;
	public static String CURRENT_TASK;
	public static String LOGS_PATH;
	public static String TEMP_PATH;
	
	/**
	 * Sets the Server Parameters.
	 * @param params
	 */
	public static void setParams(String[] params) {
		SERVER_ADDRESS = params[0];
		SERVER_PORT = Integer.parseInt(params[1]);
		N_INSTANCES = Integer.parseInt(params[2]);
		JOB_ID = "JOB-" + UUID.randomUUID().toString();
		LOGS_PATH = "logs";
		TEMP_PATH = JOB_ID + "/" + "_temp";
		// create temp directory for intermediate files and logs
		FileUtils.createDir(JOB_ID);		
		FileUtils.createDir(LOGS_PATH);
		FileUtils.createDir(TEMP_PATH);
	}

	/**
	 * Sets parameters, Initializes log and Starts the Server.
	 * @param args
	 * @throws InterruptedException
	 */
	public static void run(String[] args) throws InterruptedException {

		if(args.length < 3) { 
			System.out.println("Incomplete Arguments\n Terminating.");
			return;
		}
		ServerMain.setParams(args);
		System.setProperty("logfile.name",LOGS_PATH + "/" + "server_"+ JOB_ID + ".log");
		SortServer.start();
	}

	public static void main(String[] args) throws InterruptedException {
		ServerMain.run(args);
	}

}

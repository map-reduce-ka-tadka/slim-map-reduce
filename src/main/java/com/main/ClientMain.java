package com.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.net.SortClient;

/**
 * Main Class for Client
 * @author Abhijeet Sharma, Deepen Mehta
 * @since Apr 10, 2016
 *
 */
public class ClientMain {
	// Initialized by Client
	public static String INPUT_PATH;
	public static String OUTPUT_PATH;
	public static String CLIENT_ID;
	public static String SERVER_ADDRESS;
	public static int SERVER_PORT;
	public static String LOGS_PATH;

	// Initialized by Server
	public static String JOB_ID;
	public static Integer CLIENT_NUM;
	public static int N_INSTANCES;
	public static int CURRENT_OPCODE;
	public static String TEMP_PATH;
	public static String MAP_PATH;
	public static String SORT_PATH;
	public static String LOCAL_OUTPUT_PATH;
	public static String REDUCE_PATH;

	public static String CURRENT_FILE;
	public static String INPUT_BUCKET;
	public static String INPUT_FOLDER;
	public static String OUTPUT_BUCKET;
	public static String OUTPUT_FOLDER;
	public static final String masterAddressFile = "masterPublicAddress.txt";

	/**
	 * Sets the Client Parameters.
	 * @param params
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void setParams(String[] params) throws NumberFormatException, IOException {
		INPUT_PATH = params[0];
		if (INPUT_PATH.charAt(INPUT_PATH.length() - 1) == '/'){
			INPUT_PATH.substring(0, INPUT_PATH.length() - 1);
		}
		String[] inputPathSplit = INPUT_PATH.split("/");
		INPUT_BUCKET = inputPathSplit[2];
		INPUT_FOLDER = StringUtils.join(Arrays.asList(inputPathSplit).subList(3, inputPathSplit.length), "/");
		System.out.println("INPUT BUCKET: " + INPUT_BUCKET + " FOLDER: " + INPUT_FOLDER);

		OUTPUT_PATH  = params[1];
		if (OUTPUT_PATH.charAt(OUTPUT_PATH.length() - 1) == '/'){
			OUTPUT_PATH.substring(0, OUTPUT_PATH.length() - 1);
		}
		String[] outputPathSplit = OUTPUT_PATH.split("/");
		OUTPUT_BUCKET = outputPathSplit[2];
		OUTPUT_FOLDER = StringUtils.join(Arrays.asList(outputPathSplit).subList(3, outputPathSplit.length), "/");
		System.out.println("OUTPUT BUCKET: " + OUTPUT_BUCKET + " FOLDER: " + OUTPUT_FOLDER);
		CLIENT_ID = UUID.randomUUID().toString();
		ClientMain.readAddressFromFile();
		LOGS_PATH = "logs";
	}

	/**
	 * Sets the Server Address and Port fields from file.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void readAddressFromFile() throws NumberFormatException, IOException {
		String line = null;
		BufferedReader file = new BufferedReader(new FileReader(masterAddressFile));
		while((line = file.readLine()) != null) {
			SERVER_ADDRESS = line.split(":")[0];
			SERVER_PORT = Integer.parseInt(line.split(":")[1]);
		}
		file.close();
	}

	/**
	 * Sets parameters, Initializes log and Starts the client.
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void run(String[] args) throws NumberFormatException, IOException {
		if(args.length < 2) {
			System.out.println("Incomplete Arguments\n Terminating.");
			return;
		}
		ClientMain.setParams(args);
		System.setProperty("logfile.name", LOGS_PATH + "/" + "client_"+ CLIENT_ID + ".log");		
		SortClient.start();
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		ClientMain.run(args);
	}
}
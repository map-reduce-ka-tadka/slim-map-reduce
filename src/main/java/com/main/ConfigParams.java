/*
 * @author Mehta, Deepen  
 * @created Apr 10, 2016 
 */

package com.main;

public class ConfigParams {

	public static  String TASK;
	public static String TYPE;	
	public static String MASTER_ADDRESS;
	public static int PORT;
	public static int N_INSTANCES;
	public static String INPUT_BUCKET;
	public static String INPUT_FOLDER;
	public static String OUTPUT_BUCKET;
	public static String OUTPUT_FOLDER;
	public static String CURRENT_FILE;

	public static void setParams(String[] params) {
		TYPE = params[0];
		MASTER_ADDRESS = params[1];
		PORT = Integer.parseInt(params[2]);
		N_INSTANCES = Integer.parseInt(params[3]);
		INPUT_BUCKET= params[4];
		INPUT_FOLDER = params[5];
		OUTPUT_BUCKET= params[6];
		OUTPUT_FOLDER =params[7];
	}
}

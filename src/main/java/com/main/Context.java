package com.main;

import java.io.IOException;

import com.map.Mapper;
import com.reduce.Reducer;
import com.utils.FileUtils;

/**
 * Class for setting the Mapper class, Reducer class, Input path and Output path
 * @author Deepen
 */
public class Context {

	public static Class<? extends Mapper> mapper;
	public static Class<? extends Reducer> reducer;
	public static String inputPath;
	public static String outputPath;

	/**
	 * sets the mapper class
	 * @param mapper
	 */
	public void setMapperClass(Class<? extends Mapper> mapper) {
		Context.mapper = mapper;
	}

	/**
	 * Sets the reducer class
	 * @param reducer
	 */
	public void setReducerClass(Class<? extends Reducer> reducer) {
		Context.reducer = reducer;
	}

	/**
	 * Method to set the input path to context
	 * @param inputPath
	 */
	public void setInputPath(String inputPath) {
		Context.inputPath = inputPath;
	}

	/**
	 * Method to set the output path to context
	 * @param outputPath
	 */
	public void setOutputPath(String outputPath) {
		Context.outputPath = outputPath;
	}

	/**
	 * Method to write the output from mapper/reducer to context
	 * @param key
	 * @param value
	 */
	public static void write(String key, String value) {
		String filePath = ClientMain.CURRENT_OPCODE == 1 ? ClientMain.MAP_PATH : ClientMain.LOCAL_OUTPUT_PATH;
		FileUtils.useBufferedOutPutStream(key, value, filePath + "/" + ClientMain.CLIENT_NUM + "_" + ClientMain.CURRENT_FILE);
	}

	/**
	 * Method to check whether the current job is complete.
	 */
	public void jobWaitCompletionTrue() throws NumberFormatException, IOException {
		String args[] = new String[2];
		args[0] = Context.inputPath;
		args[1] = Context.outputPath;
		ClientMain.run(args);
	}
}

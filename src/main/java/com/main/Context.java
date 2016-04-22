package com.main;


import java.io.IOException;

import com.map.Mapper;
import com.net.SortClient;
import com.reduce.Reducer;
import com.utils.FileUtils;


public class Context {
	
	public static Class<? extends Mapper> mapper;
	public static Class<? extends Reducer> reducer;
	public static String inputPath;
	public static String outputPath;
	
	
	public void setMapperClass(Class<? extends Mapper> mapper) {
		Context.mapper = mapper;
	}

	public void setReducerClass(Class<? extends Reducer> reducer) {
		Context.reducer = reducer;
	}

	public void setInputPath(String inputPath) {
		Context.inputPath = inputPath;
	}

	public void setOutputPath(String outputPath) {
		Context.outputPath = outputPath;
	}

	public static void write(String key, String value) {
		String filePath = ClientMain.CURRENT_OPCODE == 1 ? ClientMain.MAP_PATH : ClientMain.LOCAL_OUTPUT_PATH;
		FileUtils.useBufferedOutPutStream(key, value, filePath + "/" + ClientMain.CLIENT_NUM + "_" + ClientMain.CURRENT_FILE);
	}
	
	public void jobWaitCompletionTrue() throws NumberFormatException, IOException {
		String args[] = new String[2];
		args[0] = Context.inputPath;
		args[1] = Context.outputPath;
		ClientMain.run(args);
	}
	
	

	

}

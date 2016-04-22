package com.main;


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
		FileUtils.useBufferedOutPutStream(key, value, ClientMain.CURRENT_OPCODE + "/"+ClientMain.CURRENT_FILE);
	}
	
	public void jobWaitCompletionTrue() {
		String args[] = new String[4];
		args[0] = Context.inputPath;
		args[1] = Context.outputPath;
		//ClientMain.run(args);
	}
	
	

	

}

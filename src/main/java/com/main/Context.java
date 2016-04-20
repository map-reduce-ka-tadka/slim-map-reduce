package com.main;

import com.examples.M;
import com.mr.Mapper;
import com.mr.Reducer;


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
		
	}
	
	

	

}

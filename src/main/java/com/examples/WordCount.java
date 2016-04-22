package com.examples;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import com.main.Context;
import com.map.Mapper;

public class WordCount {
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		
		Context context = new Context();
		context.setMapperClass(M.class);
		context.setReducerClass(R.class);
		context.setInputPath(args[0]);
		context.setOutputPath(args[1]);
		context.jobWaitCompletionTrue();
	}
	
}

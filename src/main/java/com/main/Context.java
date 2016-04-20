package com.main;

public abstract class Context {
	
	public Class mapper;
	public Class reducer;
	public String inputPath;
	public String outputPath;
	
	
	public void setMapper(Class mapper) {
		this.mapper = mapper;
	}

	public void setReducer(Class reducer) {
		this.reducer = reducer;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public static void write(String key, String value) {
		
	}
	
	
}

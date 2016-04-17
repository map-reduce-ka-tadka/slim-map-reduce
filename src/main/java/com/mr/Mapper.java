package com.mr;

public abstract class Mapper {

	public abstract void setup();
	public abstract void map(String key, String value);
	public abstract void cleanup();
	
}

package com.map;

import com.main.Context;

public abstract class Mapper {

	public abstract void setup();
	public abstract void map(String key, String value, Context context);
	public abstract void cleanup();
	
}

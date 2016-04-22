package com.reduce;

import com.main.Context;

public abstract class Reducer {
	
	public abstract void setup();
	public abstract void reduce(String key, Iterable<String> value, Context context);
	public abstract void cleanup();

}

package com.reduce;

import com.main.Context;

public abstract class Reducer {
	
	public abstract void setup();
	public abstract <T> void reduce(String key, Iterable<T> value, Context context);
	public abstract void cleanup();

}

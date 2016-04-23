package com.reduce;

import com.main.Context;
/**
 * Abstract Class for Reduce.java
 * @author Deepen
 */
public abstract class Reducer {
	public abstract void reduce(String key, Iterable<String> value, Context context);
}
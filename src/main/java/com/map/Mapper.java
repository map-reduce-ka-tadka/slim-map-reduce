package com.map;

import com.main.Context;
/**
 * Abstract Class for Map.java
 * @author Deepen
 */
public abstract class Mapper {
	public abstract void map(String key, String value, Context context);
}
package com.map;

import com.main.Context;
/**
 * Provides an abstraction of Mapper
 * @author Deepen
 */
public abstract class Mapper {
	public abstract void map(String key, String value, Context context);
}
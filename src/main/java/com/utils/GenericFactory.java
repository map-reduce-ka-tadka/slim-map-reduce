package com.utils;

public class GenericFactory {
	
	
	public static <T> T getInstance(Class<T> theClass)
		    throws IllegalAccessException, InstantiationException {
		    return theClass.newInstance();
		}
	

}

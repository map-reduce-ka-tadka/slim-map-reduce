package com.utils;
/**
 * Generic Factory for Mapper/Reducer Object.
 * @author Deepen Mehta
 */
public class GenericFactory {
	/**
	 * Instantiates an Object.
	 * @param theClass
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static <T> T getInstance(Class<T> theClass)
			throws IllegalAccessException, InstantiationException {
		return theClass.newInstance();
	}
}

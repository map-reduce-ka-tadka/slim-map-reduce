/**
 * @author Deepen Mehta
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.examples;

import com.main.Context;
import com.reduce.Reducer;

/**
 * Example reducer class
 */
public class R extends Reducer{

	/**
	 * Custom map method
	 */
	@Override
	public void reduce(String key, Iterable<String> value, Context context) {	
		int count = 0;	
		for(String s : value) {
			count = count + 1;
		}		
		context.write(key, String.valueOf(count));		
	}
}

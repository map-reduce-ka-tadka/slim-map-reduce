/**
 * @author Deepen Mehta
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.examples;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.main.Context;

/**
 * Example class for Word Count program. This program is used to test the Map Reduce API.
 * @author Deepen Mehta
 */
public class WordCount {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {

		Context context = new Context();
		context.setMapperClass(M.class);
		context.setReducerClass(R.class);
		context.setInputPath(args[0]);
		context.setOutputPath(args[1]);
		context.jobWaitCompletionTrue();
	}

}

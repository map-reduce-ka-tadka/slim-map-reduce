package com.examples;

import com.main.Context;
import com.reduce.Reducer;

public class R extends Reducer{

	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reduce(String key, Iterable<String> value, Context context) {
	
		int count = 0;
		
		for(String s : value) {
			count = count + 1;
		}	
		
		context.write(key, String.valueOf(count));		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
	
	

}

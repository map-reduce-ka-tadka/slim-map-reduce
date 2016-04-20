package com.examples;

import com.main.Context;
import com.map.Mapper;

public class M extends Mapper {

	@Override
	public void setup() {
		// TODO Auto-generated method stub
	}

	@Override
	public void map(String key, String value, Context context) {
		// TODO Auto-generated method stub		
		
		System.out.println("In map method");
		
		context.write(key, value);
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub		
	}

}

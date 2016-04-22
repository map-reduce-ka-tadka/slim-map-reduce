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
		context.write(value, String.valueOf(1));		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub		
	}

}

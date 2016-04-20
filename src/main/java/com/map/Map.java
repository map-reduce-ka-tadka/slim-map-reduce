package com.map;

import com.aws.AWSManager;
import com.main.Context;
import com.mr.Mapper;

public class Map {
	
	public AWSManager AWSConnect;
	Mapper mapper; 
	public Context context = new Context();
	
	public Map() throws IllegalAccessException, InstantiationException {
		this.AWSConnect = new AWSManager();
		this.mapper = getInstance(Context.mapper);
	}
	
	public void map(int clientID) {
		AWSConnect.mapAllFiles(clientID,this.mapper,context);
	}
	
	
	public static <T> T getInstance(Class<T> theClass)
		    throws IllegalAccessException, InstantiationException {

		    return theClass.newInstance();
		}
	
	
}

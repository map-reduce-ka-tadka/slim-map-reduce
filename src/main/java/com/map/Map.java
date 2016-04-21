package com.map;

import com.aws.AWSManager;
import com.main.Context;
import com.utils.GenericFactory;

public class Map {
	
	public AWSManager AWSConnect;
	Mapper mapper; 
	public Context context = new Context();
	
	public Map() throws IllegalAccessException, InstantiationException {
		this.AWSConnect = new AWSManager();
		this.mapper = GenericFactory.getInstance(Context.mapper);
	}
	
	public void map(int clientID) {
		AWSConnect.mapAllFiles(clientID,this.mapper,context);
		AWSConnect.sendFileToS3("", "");
		
		// send response to master finish of map task
	}
		
}

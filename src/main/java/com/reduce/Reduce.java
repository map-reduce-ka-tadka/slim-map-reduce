package com.reduce;

import com.aws.AWSManager;
import com.main.Context;
import com.utils.GenericFactory;

public class Reduce {
	
	public AWSManager AWSConnect;
	Reducer reducer; 
	public Context context = new Context();
	
	public Reduce() throws IllegalAccessException, InstantiationException {
		this.AWSConnect = new AWSManager();
		this.reducer = GenericFactory.getInstance(Context.reducer);
	}
	
	public void reduce(int clientID) {
		AWSConnect.reduceKey(clientID,this.reducer,context);
		
		AWSConnect.sendFileToS3("", "");
		
		// send response to master finish of reduce task
	}

}

package com.reduce;

import java.io.File;
import java.io.FileNotFoundException;

import com.aws.AWSManager;
import com.main.ClientMain;
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
	
	public void reduce(int clientNum) throws FileNotFoundException {
		AWSConnect.reduceKey(clientNum, this.reducer);		
		//AWSConnect.sendFileToS3(ClientMain.LOCAL_OUTPUT_PATH, ClientMain.LOCAL_OUTPUT_PATH);
		File outputDirectory = new File(ClientMain.LOCAL_OUTPUT_PATH);
		File[] files = outputDirectory.listFiles();
		for(File f : files) {
			if (f.getName().startsWith("" + clientNum)){
				AWSConnect.sendFileToS3(ClientMain.LOCAL_OUTPUT_PATH+"/"+f.getName(), ClientMain.LOCAL_OUTPUT_PATH+"/"+f.getName());	
			}			
		}		
		// send response to master finish of reduce task
	}
}

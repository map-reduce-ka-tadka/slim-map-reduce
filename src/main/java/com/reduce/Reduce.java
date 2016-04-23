package com.reduce;

import java.io.File;
import java.io.FileNotFoundException;

import com.aws.AWSManager;
import com.main.ClientMain;
import com.main.Context;
import com.utils.GenericFactory;

/**
 * Provides an implementation of Reduce Task
 * @author Deepen
 */
public class Reduce {
	public AWSManager AWSConnect;
	Reducer reducer; 
	public Context context = new Context();

	public Reduce() throws IllegalAccessException, InstantiationException {
		this.AWSConnect = new AWSManager();
		this.reducer = GenericFactory.getInstance(Context.reducer);
	}
	/**
	 * Implementation of reduce method for Reduce Task.
	 * @param clientNum
	 * @throws FileNotFoundException
	 */
	public void reduce(int clientNum) throws FileNotFoundException {
		AWSConnect.reduceKey(clientNum, this.reducer);		
		File outputDirectory = new File(ClientMain.LOCAL_OUTPUT_PATH);
		File[] files = outputDirectory.listFiles();
		for(File f : files) {
			if (f.getName().startsWith("" + clientNum)){
				AWSConnect.sendFileToS3(ClientMain.LOCAL_OUTPUT_PATH+"/"+f.getName(), ClientMain.LOCAL_OUTPUT_PATH+"/"+f.getName());	
			}			
		}		
	}
}
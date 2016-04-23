package com.map;

import java.io.File;

import com.aws.AWSManager;
import com.main.ClientMain;
import com.main.Context;
import com.utils.GenericFactory;

/**
 * Map Class for mapper.
 * @author Deepen
 */
public class Map {
	public AWSManager AWSConnect;
	Mapper mapper; 
	public Context context = new Context();
	public String currentFile;

	public Map() throws IllegalAccessException, InstantiationException {
		this.AWSConnect = new AWSManager();
		this.mapper = GenericFactory.getInstance(Context.mapper);
	}

	/**
	 * map method for mapper.
	 * @param clientNum
	 */
	public void map(int clientNum) {
		AWSConnect.mapAllFiles(clientNum, this.mapper);	
		File mapDirectory = new File(ClientMain.MAP_PATH);
		File[] files = mapDirectory.listFiles();
		for(File f : files) {
			if (f.getName().startsWith(""+clientNum)){
				AWSConnect.sendFileToS3(ClientMain.MAP_PATH+"/"+f.getName(), ClientMain.MAP_PATH+"/"+f.getName());	
			}			
		}
	}	
}
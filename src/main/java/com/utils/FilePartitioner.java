package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.main.ClientMain;

/**
 * Partitioned class to split files according to key
 * @author Deepen Mehta
 */
public class FilePartitioner {
	public static void partition(String filename) {
		File file = new File(filename);		
		if (file.exists()) {
			BufferedReader list;
			try {
				list = new BufferedReader (new FileReader(file));
				String initLine = list.readLine();
				while (initLine != null) {
					String[] finalSplit = initLine.split("\t");
					String key = finalSplit[0];
					String val = finalSplit[1];				
					FileUtils.writeByKey(val, ClientMain.REDUCE_PATH + "/"+ ClientMain.CLIENT_NUM + "_" + key);
					initLine = list.readLine();					
				}
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}		
	}
}

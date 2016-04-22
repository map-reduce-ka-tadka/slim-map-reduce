package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.main.ClientMain;

public class FilePartitioner {

	public static void partition(String filename) {
		System.out.println("Starting partitioner on filename: " + filename);
		File file = new File(filename);		
		if (file.exists()) {
			BufferedReader list;
			try {
				list = new BufferedReader (new FileReader(file));
				String initLine = list.readLine();
				while (initLine != null) {
					System.out.println("Initline: " + initLine);
					String[] finalSplit = initLine.split("\t");
					String key = finalSplit[0];
					String val = finalSplit[1];				
					FileUtils.writeByKey(val, ClientMain.REDUCE_PATH + "/"+ ClientMain.CLIENT_NUM + "_" + key);
					initLine = list.readLine();					
				}
				System.out.println("Completed parititoner on filename: " + filename);
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}		
	}
}

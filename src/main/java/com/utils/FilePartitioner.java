package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FilePartitioner {
	
	public static void partition(String filename) {
		File file = new File(filename);		
		if (file.exists()) {
			BufferedReader list;
			try {
				list = new BufferedReader (new FileReader(file));
				String initLine = list.readLine();
				while (initLine != null) {
					String key = initLine.substring(0, initLine.indexOf(','));
					FileUtils.useBufferedOutPutStream("", initLine, key);				
				}
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}		
	}
}

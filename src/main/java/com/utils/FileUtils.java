package com.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.sort.TemperatureInfo;

public class FileUtils {
	
	public static void useBufferedOutPutStream(String key, String value, String filePath) {
		FileOutputStream bout = null;
		try {
			bout = new FileOutputStream(filePath, true);			
				String line = key+"\t"+value;
				line += System.getProperty("line.separator");
				byte[] bytes = line.getBytes();
				bout.write(bytes);
			
		} catch (IOException e) {
		} finally {
			if (bout != null) {
				try {
					bout.close();
				} catch (Exception e) {
				}
			}
		}
	}

}

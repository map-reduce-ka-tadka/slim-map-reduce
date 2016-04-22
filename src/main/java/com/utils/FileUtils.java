package com.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

	public static void createDir(String directoryName){
		File dir = new File(String.valueOf(directoryName));
		// if the directory does not exist, create it
		if (!dir.exists()) {
			System.out.println("creating directory: " + directoryName);
			boolean successful = false;
			try{
				successful = dir.mkdir();
			}
			catch (Exception e){
				e.printStackTrace();			
			}
			if (successful)
			{
				// creating the directory succeeded
				System.out.println("directory was created successfully: " + directoryName);
			}
			else
			{
				// creating the directory failed
				System.out.println("failed trying to create the directory: " + directoryName);
			}
		}
	}
}

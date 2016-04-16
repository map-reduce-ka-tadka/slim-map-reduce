/*
 * @author Mehta, Deepen 
 * @created Apr 8, 2016 
 */

package com.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class CombineFiles
{
	public static void merger (String currentFile, String previousFile, String mergedFile) throws IOException
	{
		String filename1 = currentFile;
		String filename2 = previousFile;
		String combinedFileName = mergedFile;
		String nextName1 = "";
		String nextName2 = "";
		File file1 = new File(filename1);
		File file2 = new File (filename2);
		if (!file1.exists()) {
			System.out.println(file1 + " does not exist.\nTerminating Program.");
			System.exit(0);
		}
		if (!file2.exists()) {
			System.out.println(file2 + " does not exist.\nTerminating Program.");
			System.exit(0);
		}   
		File combinedFile = new File (combinedFileName);
		if (file1.exists() && file2.exists()) {
			BufferedReader list1 = new BufferedReader (new FileReader(file1));
			BufferedReader list2 = new BufferedReader (new FileReader(file2)); 
			PrintWriter outputFile = new PrintWriter(combinedFile);
			nextName1 = list1.readLine();
			nextName2 = list2.readLine();
			while (nextName1 != null || nextName2 !=null ) {
				if(nextName1 != null && nextName2 != null) {
					String[] fileLine1 = nextName1.split(",");
					String[] fileLine2 = nextName2.split(",");
					try {
						Double f1 = Double.parseDouble(fileLine1[3]);
						Double f2 = Double.parseDouble(fileLine2[3]);
						if(f1 <= f2) {
							outputFile.println(nextName1);
							nextName1 = list1.readLine();
						} else {
							outputFile.println(nextName2);
							nextName2 = list2.readLine();
						}
					}  catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
						System.out.println(nextName1);
						System.out.println(nextName2);
						nextName1 = list1.readLine();
						nextName2 = list2.readLine();
					}
				}
				else if(nextName1 != null && nextName2 == null) {
					outputFile.println(nextName1);
					nextName1 = list1.readLine();

				}
				else if(nextName1 == null && nextName2 != null) {
					outputFile.println(nextName2);
					nextName2 = list2.readLine();
				}
			}
			outputFile.close();			
		}
	}
}   
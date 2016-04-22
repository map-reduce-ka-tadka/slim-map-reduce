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

public class FileMerger
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
		/*		if (!file1.exists()) {
			System.out.println(file1 + " does not exist.\nTerminating Program.");
			System.exit(0);
		}
		if (!file2.exists()) {
			System.out.println(file2 + " does not exist.\nTerminating Program.");
			System.exit(0);
		}*/   
		File combinedFile = new File (combinedFileName);
		if (file1.exists() && file2.exists()) {
			BufferedReader list1 = new BufferedReader (new FileReader(file1));
			BufferedReader list2 = new BufferedReader (new FileReader(file2)); 
			PrintWriter outputFile = new PrintWriter(combinedFile);
			nextName1 = list1.readLine();
			nextName2 = list2.readLine();
			while (nextName1 != null || nextName2 !=null ) {
				if(nextName1 != null && nextName2 != null) {
					String[] fileLine1 = nextName1.split("\t");
					String[] fileLine2 = nextName2.split("\t");
					try {
						String f1 = fileLine1[0];
						String f2 = fileLine2[0];
						System.out.println("Inside merge, f1: " + f1 + " f2: " + f2);
						System.out.println("compare: " + f1.compareToIgnoreCase(f2));
						if(f1.compareToIgnoreCase(f2) <= 0) {
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
					System.out.println("nextName2 is null. nextName1: " + nextName1);
					outputFile.println(nextName1);
					nextName1 = list1.readLine();

				}
				else if(nextName1 == null && nextName2 != null) {
					System.out.println("nextName1 is null. nextName2: " + nextName2);
					outputFile.println(nextName2);
					nextName2 = list2.readLine();
				}				
			}
			System.out.println("Everybody's done");
			outputFile.close();
			list1.close();
			list2.close();
		}
	}
}   
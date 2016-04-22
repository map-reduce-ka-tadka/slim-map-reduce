/*
 * @author Abhijeet Sharma, Deepen Mehta
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.sort;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.aws.AWSManager;
import com.main.ClientMain;
import com.main.ServerMain;
import com.utils.FilePartitioner;
import com.utils.FileUtils;


public class SampleSort {
	public AWSManager AWSConnect;
	public ArrayList<SortObject> sortRecords;

	public SampleSort() {
		this.AWSConnect = new AWSManager();
	}

	public String readAndSampleData(int clientId) throws IOException{
		/* Start of Phase 1 (Read, Sort and Sample Local Data) (Run by Client)  */		
		// fetch given files and store in Client Memory
		this.sortRecords = this.AWSConnect.getAllFiles(clientId);
		// sort data
		Collections.sort(this.sortRecords, new SortComparator());
		// sample data
		int n = this.sortRecords.size() * ClientMain.N_INSTANCES;
		System.out.println("Total Records Read: " + n);
		int p = ClientMain.N_INSTANCES;
		int w = n / (p * p);
		System.out.println("n: " + n + " p: " + p + " w: " + w);
		String samples = getSamples(this.sortRecords, w, p);        
		return samples;
	}

	public String getSamples(ArrayList<SortObject> sortRecords, int w, int p) {
		TreeSet<String> regularSample = new TreeSet<String>();
		for (int i = 0; i <= (p - 1) * w; i = i + w) {
			regularSample.add(sortRecords.get(i).getKey());
		}
		System.out.println("Samples size: " + regularSample.size());
		String samples = StringUtils.join(regularSample.toArray(),",");
		System.out.println("samples: " + samples);
		return samples;
	}

	public static String fetchPivotsFromSamples(String concatSamples) {
		/* Start of Phase 2 : Find Pivots (Run by Server) */
		String pivots="";
		if (concatSamples.length() > 0) {
			String[] sampleArray=new ArrayList<String>() {/**
			 * 
			 */
				private static final long serialVersionUID = 1L;

				{
					for (String sample :  concatSamples.split(",")) 
						add(new String(sample));}}.toArray(new String[concatSamples.split(",").length]);
						int rho = ServerMain.N_INSTANCES / 2;
						Arrays.sort(sampleArray);
						for(int i = ServerMain.N_INSTANCES; i <= (ServerMain.N_INSTANCES*(ServerMain.N_INSTANCES-1)) + rho; i = i+ServerMain.N_INSTANCES) {
							pivots += "," + sampleArray[i];
						}        
		}
		return pivots.substring(1);
	}

	public void partitionAndUploadData(String pivots, int ClientId){
		/* Start of Phase 3 : Partition and Exchange (Run by Client) */
		String[] pivotList = new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				for (String pivot :  pivots.split(",")) add(new String(pivot));
			}}.toArray(new String[pivots.split(",").length]);			
			System.out.println("Pivots: " + pivotList.length);		
			HashMap<String, ArrayList<SortObject>> dataMap = new HashMap<String, ArrayList<SortObject>>();

			for (SortObject t : sortRecords) SortObject.upsertData(dataMap, t);
			//TreeMap<String, ArrayList<SortObject>> sortedMap = new TreeMap<String, ArrayList<SortObject>>(String.CASE_INSENSITIVE_ORDER);
			Comparator<String> stringComparator = new Comparator<String>() {
		        @Override public int compare(String s1, String s2) {
		        	if (s1.equalsIgnoreCase(s2)){
		        		return s1.compareTo(s2);
		        	}
		        	else{
		        		return s1.compareToIgnoreCase(s2);
		        	}
		        }           
		    };
			TreeMap<String, ArrayList<SortObject>> sortedMap = new TreeMap<String, ArrayList<SortObject>>(stringComparator);
			for (String key: dataMap.keySet()){
				sortedMap.put(key, dataMap.get(key));
			}
			sortRecords = null;
			dataMap = null;        
			System.out.println("sortedMap: " + sortedMap.entrySet().toString());
			int i = 0;
			
			for (String key : sortedMap.keySet()) {
				if (i < pivotList.length) {
					//if (key > pivotList[i]) {
					System.out.println("key: " + key + " pivot: " + pivotList[i] + " compare: " + key.compareTo(pivotList[i]));
					if (key.compareToIgnoreCase(pivotList[i]) > 0) {
						String uploadFileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "_" + ClientId;
						String ec2FileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "/" + ClientId;
						System.out.println("Sending to S3: " + uploadFileName);
						this.AWSConnect.sendFileToS3(uploadFileName, ec2FileName); 
						i++;
					}
				}
				FileUtils.useBufferedOutPutStream(sortedMap.get(key), ClientMain.SORT_PATH + "/" + String.valueOf(i) + "_" + ClientId);
			}
			// for the last partition
			String uploadFileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "_" + ClientId;
			String ec2FileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "/" + ClientId;
			System.out.println("Sending to last S3: " + uploadFileName);
			this.AWSConnect.sendFileToS3(uploadFileName, ec2FileName); 

	}

	public void phaseFour(int clientId) throws FileNotFoundException, IOException{
		/* Start of Phase 4 (Merge Partitions) */
		String concatFileName = this.AWSConnect.readFilesfromS3andConcat(clientId);
		System.out.println("Client: " + ClientMain.CLIENT_NUM + " has completed concatenating files.Starting Partitioner..");
		ClientMain.REDUCE_PATH = ClientMain.TEMP_PATH + "/" + "reduce";
		FileUtils.createDir(ClientMain.REDUCE_PATH);
		FilePartitioner.partition(concatFileName);
		System.out.println("Partitioner complete");
		//String s3name = concatFileName.replace(clientId + "/", "");
		// write to s3 appending with Sort Node Name
		///this.AWSConnect.sendFileToS3(concatFileName, ClientMain.OUTPUT_FOLDER + "/" + s3name);
		/* End of Phase 4 */
	}

}

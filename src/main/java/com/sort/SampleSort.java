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

/**
 * This class generates samples data, fetches pivots from samples, partitions and uploads data to S3.
 * @author Deepen Sharma, Abhijeet Sharma
 */
public class SampleSort {
	public AWSManager AWSConnect;
	public ArrayList<SortObject> sortRecords;

	/**
	 * Constructor for Sample Sort
	 */
	public SampleSort() {
		this.AWSConnect = new AWSManager();
	}

	/**
	 * Phase 1 (Read, Sort and Sample Local Data) (Run by Client)
	 * This method reads the files from S3 and generates samples 
	 * @param clientId
	 * @return
	 * @throws IOException
	 */
	public String readAndSampleData(int clientId) throws IOException{
		// fetch given files and store in Client Memory
		this.sortRecords = this.AWSConnect.getAllFiles(clientId);
		// sort data
		Collections.sort(this.sortRecords, new SortComparator());
		// sample data
		int n = this.sortRecords.size() * ClientMain.N_INSTANCES;
		int p = ClientMain.N_INSTANCES;
		int w = n / (p * p);
		String samples = getSamples(this.sortRecords, w, p);        
		return samples;
	}

	/**
	 * This method is to get the samples from the sorted records.
	 * @param sortRecords
	 * @param w
	 * @param p
	 * @return
	 */
	public String getSamples(ArrayList<SortObject> sortRecords, int w, int p) {
		TreeSet<String> regularSample = new TreeSet<String>();
		for (int i = 0; i <= (p - 1) * w; i = i + w) {
			regularSample.add(sortRecords.get(i).getKey());
		}
		String samples = StringUtils.join(regularSample.toArray(),",");
		return samples;
	}

	/**
	 * Phase 2 : Find Pivots (Run by Server)
	 * This method is to create pivots from the generated samples
	 * @param concatSamples
	 * @return
	 */
	public static String fetchPivotsFromSamples(String concatSamples) {
		String pivots="";
		if (concatSamples.length() > 0) {
			String[] sampleArray=new ArrayList<String>() {
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

	/**
	 * Phase 3 : Partition and Exchange (Run by Client)
	 * Partitions the sorted records according to the pivots and uploads the data to S3
	 * @param pivots
	 * @param ClientId
	 */
	public void partitionAndUploadData(String pivots, int ClientId){
		String[] pivotList = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				for (String pivot :  pivots.split(",")) add(new String(pivot));
			}}.toArray(new String[pivots.split(",").length]);	
			HashMap<String, ArrayList<SortObject>> dataMap = new HashMap<String, ArrayList<SortObject>>();
			for (SortObject t : sortRecords) SortObject.upsertData(dataMap, t);
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
			int i = 0;
			for (String key : sortedMap.keySet()) {
				if (i < pivotList.length) {
					if (key.compareToIgnoreCase(pivotList[i]) > 0) {
						String uploadFileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "_" + ClientId;
						String ec2FileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "/" + ClientId;
						this.AWSConnect.sendFileToS3(uploadFileName, ec2FileName); 
						i++;
					}
				}
				FileUtils.useBufferedOutPutStream(sortedMap.get(key), ClientMain.SORT_PATH + "/" + String.valueOf(i) + "_" + ClientId);
			}
			// for the last partition
			String uploadFileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "_" + ClientId;
			String ec2FileName =  ClientMain.SORT_PATH + "/" + String.valueOf(i) + "/" + ClientId;
			this.AWSConnect.sendFileToS3(uploadFileName, ec2FileName); 

	}

	/**
	 * Phase 4 (Merge Partitions)
	 * This method reads the files from S3 and concatenates them
	 * @param clientId
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void phaseFour(int clientId) throws FileNotFoundException, IOException{
		String concatFileName = this.AWSConnect.readFilesfromS3andConcat(clientId);
		ClientMain.REDUCE_PATH = ClientMain.TEMP_PATH + "/" + "reduce";
		FileUtils.createDir(ClientMain.REDUCE_PATH);
		FilePartitioner.partition(concatFileName);
	}
}
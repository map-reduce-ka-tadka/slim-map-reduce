/*
 * @author Abhijeet Sharma, Deepen Mehta
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.sort;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.aws.AWSManager;
import com.main.ConfigParams;


public class SampleSort {
	public AWSManager AWSConnect;
	public ArrayList<TemperatureInfo> tempRecords;

	public SampleSort() {
		this.AWSConnect = new AWSManager();
	}

	public String readAndSampleData(int clientId) throws IOException{
		/* Start of Phase 1 (Read, Sort and Sample Local Data) (Run by Client)  */		
		// fetch given files and store in Client Memory
		this.tempRecords = this.AWSConnect.getAllFiles(clientId);
		// sort data
		Collections.sort(this.tempRecords, new TemperatureComparator());
		// sample data
		int n = this.tempRecords.size() * ConfigParams.N_INSTANCES;
		System.out.println("Total Records Read: " + n);
		int p = ConfigParams.N_INSTANCES;
		int w = n / (p * p);
		System.out.println("n: " + n + " p: " + p + " w: " + w);
		String samples = getSamples(this.tempRecords, w, p);        
		return samples;
	}

	public String getSamples(ArrayList<TemperatureInfo> tempRecords, int w, int p) {
		TreeSet<Double> regularSample = new TreeSet<Double>();
		for (int i = 0; i <= (p - 1) * w; i = i + w) {
			regularSample.add(tempRecords.get(i).getTemperature());
		}
		System.out.println("Samples: " + regularSample.size());
		String samples = StringUtils.join(regularSample.toArray(),",");
		return samples;
	}

	public static String fetchPivotsFromSamples(String concatSamples) {
		/* Start of Phase 2 : Find Pivots (Run by Server) */
		String pivots="";
		if (concatSamples.length() > 0) {
			Double[] sampleArray=new ArrayList<Double>() {/**
			 * 
			 */
				private static final long serialVersionUID = 1L;

				{
					for (String sample :  concatSamples.split(",")) 
						add(new Double(sample));}}.toArray(new Double[concatSamples.split(",").length]);
						int rho = ConfigParams.N_INSTANCES / 2;
						Arrays.sort(sampleArray);
						for(int i = ConfigParams.N_INSTANCES; i <= (ConfigParams.N_INSTANCES*(ConfigParams.N_INSTANCES-1)) + rho; i = i+ConfigParams.N_INSTANCES) {
							pivots += "," + sampleArray[i];
						}        
		}
		return pivots.substring(1);
	}

	public void partitionAndUploadData(String pivots, int ClientId){
		/* Start of Phase 3 : Partition and Exchange (Run by Client) */
		Double[] pivotList = new ArrayList<Double>() {/**
		 * 
		 */
			private static final long serialVersionUID = 1L;

			{
				for (String pivot :  pivots.split(",")) add(new Double(pivot));
			}}.toArray(new Double[pivots.split(",").length]);			
			System.out.println("Pivots: " + pivotList.length);		
			HashMap<Double, ArrayList<TemperatureInfo>> dataMap = new HashMap<Double, ArrayList<TemperatureInfo>>();

			for (TemperatureInfo t : tempRecords) TemperatureInfo.upsertData(dataMap, t);
			TreeMap<Double, ArrayList<TemperatureInfo>> sortedMap = new TreeMap<Double, ArrayList<TemperatureInfo>>(dataMap);
			tempRecords = null;
			dataMap = null;        
			//System.out.println("Unique Keys(sorted): " + sortedMap.keySet().size());
			int i = 0;
			for (Double key : sortedMap.keySet()) {
				if (i < pivotList.length) {
					if (key > pivotList[i]) {
						String uploadFileName =  String.valueOf(i) + "_" + ClientId;
						String ec2FileName =  String.valueOf(i) + "/" + ClientId;
						this.AWSConnect.sendFileToS3(uploadFileName, ec2FileName); 
						i++;
					}
				}
				useBufferedOutPutStream(sortedMap.get(key), String.valueOf(i) + "_" + ClientId);
			}
			// for the last partition
			String uploadFileName =  String.valueOf(i) + "_" + ClientId;
			String ec2FileName =  String.valueOf(i) + "/" + ClientId;
			this.AWSConnect.sendFileToS3(uploadFileName, ec2FileName); 

	}

	public void useBufferedOutPutStream(List<TemperatureInfo> content, String filePath) {
		FileOutputStream bout = null;
		try {
			bout = new FileOutputStream(filePath, true);
			for (TemperatureInfo t : content) {
				String line = t.toString();
				line += System.getProperty("line.separator");
				byte[] bytes = line.getBytes();
				bout.write(bytes);
			}
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
	public void phaseFour(int clientId) throws FileNotFoundException, IOException{
		/* Start of Phase 4 (Merge Partitions) */
		String concatFileName = this.AWSConnect.readFilesfromS3andConcat(clientId);
		String s3name = concatFileName.replace(clientId+"/", "");
		// write to s3 appending with Sort Node Name
		this.AWSConnect.sendFileToS3(concatFileName, ConfigParams.OUTPUT_FOLDER+"/" + s3name);
		/* End of Phase 4 */
	}

}

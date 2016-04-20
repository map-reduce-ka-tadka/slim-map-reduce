/*
 * @author Abhijeet Sharma, Deepen Mehta
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.aws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.google.common.collect.Lists;
import com.main.ConfigParams;
import com.sort.TemperatureInfo;
import com.utils.CombineFiles;

import au.com.bytecode.opencsv.CSVParser;

public class AWSManager {
	AmazonS3 s3;	
	final static short MAX_RETRY = 3;
	public AWSManager() {
		this.s3 = configureS3();
	}

	// Default region is US_EAST_1
	public AmazonS3 configureS3(){
		/*
		 * Create your credentials file at ~/.aws/credentials (C:\Users\USER_NAME\.aws\credentials for Windows users)
		 * and save the following lines after replacing the underlined values with your own.
		 *
		 * [default]
		 * aws_access_key_id = YOUR_ACCESS_KEY_ID
		 * aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (~/.aws/credentials), and is in valid format.",
							e);
		}
		AmazonS3 s3 = new AmazonS3Client(credentials);
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		s3.setRegion(usEast1);
		return s3;
	}

	public ArrayList<TemperatureInfo> getAllFiles(int clientId)  {
		ArrayList<TemperatureInfo> temperatureRecords = new ArrayList<TemperatureInfo>();
		short retryCount = 0;
		boolean retry = false;
		/* (Run by Client) */
		do{
			try {
				// fetching filenames depending on count
				ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
						.withBucketName(ConfigParams.INPUT_BUCKET)
						.withPrefix(ConfigParams.INPUT_FOLDER + "/")
						.withDelimiter("/"));
				TreeMap<Long, String> filenamesMap = new TreeMap<Long, String>();
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					filenamesMap.put(objectSummary.getSize(), objectSummary.getKey());			
					/* System.out.println(" - " + objectSummary.getKey() + "  " +
							"(size = " + objectSummary.getSize() + ")");*/
				}
				TreeSet<String> filenamesTree = new TreeSet<String>(filenamesMap.values());
				filenamesTree.remove("climate/");

				// save all filenames to a list
				ArrayList<String> fileList = new ArrayList<String>();
				fileList.addAll(filenamesTree);

				System.out.println("Total files: " + fileList.size());
				List<List<String>> listPartitions = Lists.partition(fileList, ConfigParams.N_INSTANCES);
				List<List<String>> subElements = listPartitions.stream().limit(2).collect(Collectors.toList());
				System.out.println("No. of partitions in listPartitions: " + listPartitions.size());
				String filenames = "";
				/*for (List<String> elementList : listPartitions){
					if (elementList.size() > clientId){
						filenames = String.join(",", filenames, elementList.get(clientId));
					}
				}*/
				for (List<String> elementList : subElements){
					if (elementList.size() > clientId){
						filenames = String.join(",", filenames, elementList.get(clientId));
					}
				}

				filenames = filenames.substring(1);
				System.out.println("From client, filenames: " + filenames);
				// fetching only part of actual data
				String[] filenamesList = filenames.split(",");

				int counter = 0;
				System.out.printf("From Client, Getting all files from s3://%s/%s with instances %s", 
						ConfigParams.INPUT_BUCKET, ConfigParams.INPUT_FOLDER, ConfigParams.N_INSTANCES);
				for (String filename : filenamesList) {
					System.out.println("File counter: " + counter);            
					System.out.println("filename: " + filename);
					S3Object s3object = this.s3.getObject(new GetObjectRequest(ConfigParams.INPUT_BUCKET, filename));
					BufferedReader reader;
					try {
						reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(s3object.getObjectContent())));
						String line;
						while ((line = reader.readLine()) != null) {
							CSVParser csvParser = new CSVParser(',', '"');
							String[] parsedString;
							parsedString = csvParser.parseLine(line);
							TemperatureInfo tempInfo = TemperatureInfo.createInstance(parsedString);
							if (tempInfo != null) {
								temperatureRecords.add(tempInfo);
							}
						}
					} catch (IOException e) {
					}
					counter += 1;
				}
			}
			catch (AmazonServiceException ase) {
				System.out.println("Caught an AmazonServiceException, which " +
						"means your request made it " +
						"to Amazon S3, but was rejected with an error response" +
						" for some reason.");
				System.out.println("Error Message:    " + ase.getMessage());
				System.out.println("HTTP Status Code: " + ase.getStatusCode());
				System.out.println("AWS Error Code:   " + ase.getErrorCode());
				System.out.println("Error Type:       " + ase.getErrorType());
				System.out.println("Request ID:       " + ase.getRequestId());
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			}  
			catch (AmazonClientException ace) {
				System.out.println("Caught an AmazonClientException, which " +
						"means the client encountered " +
						"an internal error while trying to " +
						"communicate with S3, " +
						"such as not being able to access the network.");
				System.out.println("Error Message: " + ace.getMessage());
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			}
		}
		while(retry);		
		return temperatureRecords;
	}
	public void sendFileToS3(String uploadFileName, String ec2FileName){
		short retryCount = 0;
		short MAX_RETRY = 3;
		boolean retry = false;
		do{
			try {
				System.out.println("Uploading File to S3 from a file\n");
				File file = new File(uploadFileName);
				this.s3.putObject(new PutObjectRequest(ConfigParams.OUTPUT_BUCKET, ec2FileName, file));

			} 
			catch (AmazonServiceException ase) {
				System.out.println("Caught an AmazonServiceException, which " +
						"means your request made it " +
						"to Amazon S3, but was rejected with an error response" +
						" for some reason.");
				System.out.println("Error Message:    " + ase.getMessage());
				System.out.println("HTTP Status Code: " + ase.getStatusCode());
				System.out.println("AWS Error Code:   " + ase.getErrorCode());
				System.out.println("Error Type:       " + ase.getErrorType());
				System.out.println("Request ID:       " + ase.getRequestId());
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			} 
			catch (AmazonClientException ace) {
				System.out.println("Caught an AmazonClientException, which " +
						"means the client encountered " +
						"an internal error while trying to " +
						"communicate with S3, " +
						"such as not being able to access the network.");
				System.out.println("Error Message: " + ace.getMessage());
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			}
		}
		while(retry);
	}

	public String readFilesfromS3andConcat(int clientId) throws FileNotFoundException, IOException {
		short retryCount = 0;
		short MAX_RETRY = 3;
		boolean retry = false;
		do{
			try {
				//String concatenatedFilename = null;
				// creating a folder
				File dir = new File(String.valueOf(clientId));
				boolean successful = dir.mkdir();
				if (successful)
				{
					// creating the directory succeeded
					System.out.println("directory was created successfully" + clientId);
				}
				else
				{
					// creating the directory failed
					System.out.println("failed trying to create the directory" + clientId);
				}
				ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
						.withBucketName(ConfigParams.OUTPUT_BUCKET)
						.withPrefix(clientId + "/")
						.withDelimiter("/"));
				TreeSet<String> filenamesTree = new TreeSet<String>();
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					filenamesTree.add(objectSummary.getKey());			
					System.out.println(" - " + objectSummary.getKey() + "  " +
							"(size = " + objectSummary.getSize() + ")");
				}
				filenamesTree.remove(clientId+"/");
				int counter = 0;
				ArrayList<String> localfileNames = new ArrayList<>();
				System.out.println("Downloading an object");
				for (String filename : filenamesTree) {
					System.out.println("File counter: " + counter);            
					System.out.println("filename: " + filename);
					S3Object s3object = this.s3.getObject(new GetObjectRequest(ConfigParams.OUTPUT_BUCKET, filename));
					try {
						localfileNames.add(filename.split("/")[1]);
						S3ObjectInputStream objectContent = s3object.getObjectContent();
						IOUtils.copy(objectContent, new FileOutputStream(clientId+"/"+filename.split("/")[1]));
					} catch (IOException e) {
					}
					counter += 1;
				}
				System.out.println(filenamesTree.toString());
				System.out.println(localfileNames.toString());

				int x = 0;
				while (x < ConfigParams.N_INSTANCES)
				{
					if(x == 0){
						System.out.println("Merging ..... ");
						System.out.println(clientId+"/"+localfileNames.get(1) +"....." +clientId+"/"+localfileNames.get(0));
						CombineFiles.merger(clientId+"/"+localfileNames.get(1),clientId+"/"+localfileNames.get(0),clientId+"/"+"finalPart-"+clientId+"-1");
						x+=2;
					}
					else{
						System.out.println("Merging ..... ");
						System.out.println(clientId+"/"+localfileNames.get(x) +"....." +clientId+"/"+"finalPart-"+clientId+"-"+(x-1));
						CombineFiles.merger(clientId+"/"+localfileNames.get(x),clientId+"/"+"finalPart-"+clientId+"-"+(x-1),clientId+"/"+"finalPart-"+clientId+"-"+x);
						x+=1;
					}

				}
				return clientId + "/" + "finalPart-" + clientId + "-" + (x-1);
			} 
			catch (ArrayIndexOutOfBoundsException ae) {
				ae.printStackTrace();
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			}
			catch (AmazonServiceException ase) {
				System.out.println("Caught an AmazonServiceException, which " +
						"means your request made it " +
						"to Amazon S3, but was rejected with an error response" +
						" for some reason.");
				System.out.println("Error Message:    " + ase.getMessage());
				System.out.println("HTTP Status Code: " + ase.getStatusCode());
				System.out.println("AWS Error Code:   " + ase.getErrorCode());
				System.out.println("Error Type:       " + ase.getErrorType());
				System.out.println("Request ID:       " + ase.getRequestId());
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			} 
			catch (AmazonClientException ace) {
				System.out.println("Caught an AmazonClientException, which " +
						"means the client encountered " +
						"an internal error while trying to " +
						"communicate with S3, " +
						"such as not being able to access the network.");
				System.out.println("Error Message: " + ace.getMessage());
				if (retryCount == MAX_RETRY){
					retry = false;
					System.out.println("Max Retry Limit achieved.Aborting Program: " + retryCount);
				}
				else{
					retry = true;
					retryCount += 1;
					System.out.println("Retrying step. Retry Count: " + retryCount);
				}
			}
		}
		while(retry);
		return null;		
	}

	/*public static void main(String[] args) throws FileNotFoundException, IOException {
		AWSManager aws = new AWSManager();
		aws.readFilesfromS3andConcat(0);
	}*/

}

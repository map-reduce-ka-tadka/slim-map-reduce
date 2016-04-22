package com.aws;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.main.ClientMain;
import com.sort.TemperatureInfo;


public class TestJUnitAWSManager {
	
   String message = "Hello World";	
   AWSManager aws = new AWSManager();
  

   @Test
   public void testendFileToS3() {
	  // assertEquals(1,1);
	   
	   ClientMain.INPUT_BUCKET = "a9output";
	   ClientMain.INPUT_FOLDER = "output";
	   ClientMain.OUTPUT_BUCKET = "a9output";
	   
	   //File file = new File("abc");
	 
	   aws.sendFileToS3("inputFiles/abc","inputFiles/xyz");
	   
	   ObjectListing objectListing = aws.s3.listObjects(new ListObjectsRequest()
				.withBucketName(ClientMain.INPUT_BUCKET)
				.withPrefix(ClientMain.INPUT_FOLDER + "/")
				.withDelimiter("/"));
	   
	   assertNotNull(objectListing);
	  
	 //this.s3.putObject(new PutObjectRequest(ConfigParams.OUTPUT_BUCKET, "xyz", file));
   }
   
   @Test
   public void testendFolderToS3() {
	  // assertEquals(1,1);
	   
	   ClientMain.INPUT_BUCKET = "a9output";
	   ClientMain.INPUT_FOLDER = "output";
	   ClientMain.OUTPUT_BUCKET = "a9output";
	   
	   //File file = new File("abc");
	 
	   aws.sendFileToS3("inputFiles","inputFiles/xyz");
	   
	   ObjectListing objectListing = aws.s3.listObjects(new ListObjectsRequest()
				.withBucketName(ClientMain.INPUT_BUCKET)
				.withPrefix(ClientMain.INPUT_FOLDER + "/")
				.withDelimiter("/"));
	   
	   assertNotNull(objectListing);
	  
	 //this.s3.putObject(new PutObjectRequest(ConfigParams.OUTPUT_BUCKET, "xyz", file));
   }
}
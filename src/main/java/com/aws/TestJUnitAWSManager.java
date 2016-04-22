package com.aws;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.main.ClientMain;


public class TestJUnitAWSManager {
	
   String message = "Hello World";	
   AWSManager aws = new AWSManager();
  

   @Test
   public void testendFileToS3() {
	  // assertEquals(1,1);
	   
	   ClientMain.INPUT_BUCKET = "bucketfora2";
	   ClientMain.INPUT_FOLDER = "input";
	   ClientMain.OUTPUT_BUCKET = "bucketfora9";
	   
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
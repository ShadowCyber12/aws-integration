package com.s3.examples.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;


@Service
public class BucketService {



	@Autowired
	private FileStore fileStore;

	private Logger logger = LogManager.getLogger(this.getClass().getName());

	/**
	 * Method will download file from S3 and will push content to CloudWatch Logs
	 * 
	 * @param fileName
	 */
	public void downloadFile(String fileName, S3Client s3Client,String bucketName) {
		try {
			logger.info("File to be fetched from S3 {}", fileName);
			GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

			try(ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request);
		             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
				   String content = reader.lines().collect(Collectors.joining("\n"));
				   logger.info("Content {}", content);
			}
			

		} catch (IOException e) {
			logger.error("Error in reading file content {}", e.getMessage());
		} catch (S3Exception  s3Exception) {
			logger.error("Some error occured", s3Exception.getMessage());
		}

	}

	/**
	 * Calls FileStore.java class to create bucket on AWS S3
	 * 
	 * @param bucketName
	 * @return
	 */
	public String createBucket(String bucketName) {
		return fileStore.createBucket(bucketName);
	}

	/**
	 * Calls FileStore.java upload file on AWS S3, first validates file is empty if
	 * then throw Excepiton
	 * 
	 * @param file
	 * @return
	 */
	public String uploadFile(MultipartFile file, String bucketName) {
		if (file.isEmpty()) {
			throw new IllegalStateException("Cannot upload empty file");
		}
		try {
			fileStore.uploadFiletoBucket(file, bucketName);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to upload file", e);
		}
		return "File Uploaded Successfully";
	}

	public String deleteBucket(String bucketName) {
		fileStore.deleteBucket(bucketName);
		return "Bucket deleted successfully";
	}

	public String deleteFile(String bucketName, String fileName) {
		fileStore.deletFile(bucketName,fileName);
		return "File deleted successfully";
	}

}

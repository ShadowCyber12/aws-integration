package com.s3.examples.service;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Actual class that communicates with S3 to create bucket and upload file
 * 
 * @author Sk Babu Molla
 *
 */
@Service
public class FileStore {

	Logger logger = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private S3Client amazonS3;

	/**
	 * Validates that credentials are valid and bucket already exist or not
	 * 
	 * @param bucketName
	 * @return
	 */
	public String createBucket(String bucketName) {
		logger.info("Inside method createBucket");
		try {
			if (bucketAlreadyExists(bucketName)) {
				logger.error("bucket already exist");
			}
			CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();

			amazonS3.createBucket(createBucketRequest);
		} catch (S3Exception s3Exception) {
			logger.error("Unable to create bucket :" + s3Exception.getMessage());
		}
		return "Bucket created with name:" + bucketName;
	}

	/**
	 * Method validates that bucket already exists or not
	 * 
	 * @param bucketName
	 * @return
	 */
	private boolean bucketAlreadyExists(String bucketName) {
		logger.info("Checking if bucket exists: {}", bucketName);
		try {
			HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
			amazonS3.headBucket(headBucketRequest);
			return true;
		} catch (S3Exception e) {
			return false;
		}
	}

	/**
	 * Method that upload file on S3 Bucket
	 * 
	 * @param multiPart
	 * @throws Exception
	 */
	public void uploadFiletoBucket(MultipartFile multipartFile, String bucketName) throws Exception {
		logger.info("Inside method uploadFiletoBucket");
		File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
		multipartFile.transferTo(convFile);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(convFile.getName())
				.build();

		amazonS3.putObject(putObjectRequest, convFile.toPath());
		logger.info("File uploaded: {}", convFile.getName());
	}

	public void deleteBucket(String bucketName) {
		DeleteBucketRequest request = DeleteBucketRequest.builder().bucket(bucketName).build();
		amazonS3.deleteBucket(request);
		logger.info("Bucket deleted: {}", bucketName);
	}

	public void deletFile(String bucketName, String fileName) {
		DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build();
		amazonS3.deleteObject(request);
		logger.info("File deleted: {}", fileName);
	}

}

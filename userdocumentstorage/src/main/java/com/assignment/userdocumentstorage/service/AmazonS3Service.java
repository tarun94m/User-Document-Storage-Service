package com.assignment.userdocumentstorage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AmazonS3Service {
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public AmazonS3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public PutObjectResult upload(String fileName,
                                  Optional<Map<String, String>> optionalMetaData,
                                  InputStream inputStream) {

        ObjectMetadata objectMetadata = new ObjectMetadata();

        optionalMetaData.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        return amazonS3.putObject(bucketName, fileName, inputStream, objectMetadata);
    }

    public S3Object download(String userName, String fileName) {
        String key = userName+"/"+fileName;
        if(amazonS3.doesObjectExist(bucketName,key)){
            return amazonS3.getObject(bucketName,key);
        }
        return null;
    }

    public List<String> search(String userName) {
        ObjectListing listing = amazonS3.listObjects( bucketName, userName );
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = amazonS3.listNextBatchOfObjects (listing);
            summaries.addAll (listing.getObjectSummaries());
        }
        return summaries.stream().map(s3ObjectSummary -> s3ObjectSummary.getKey()).collect(Collectors.toList());
    }
}

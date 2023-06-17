package com.assignment.userdocumentstorage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(SpringRunner.class)
public class AmazonS3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    private AmazonS3Service amazonS3Service;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        amazonS3Service = new AmazonS3Service(amazonS3);
        ReflectionTestUtils.setField(amazonS3Service, "bucketName", "dummy");
    }

    @Test
    public void testSearch() {
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setKey("user/Dummy");
        ObjectListing objectListing = new ObjectListing();
        objectListing.getObjectSummaries().addAll(List.of(s3ObjectSummary));
        Mockito.when(amazonS3.listObjects(Mockito.anyString(), Mockito.anyString())).thenReturn(objectListing);
        assertEquals(amazonS3Service.search("user"), List.of("user/Dummy"));
    }

    @Test
    public void testDownloadWhenObjectDoesNotExists() {
        Mockito.when(amazonS3.doesObjectExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        assertNull(amazonS3Service.download("user", "fileName"));
    }

    @Test
    public void testDownloadWhenObjectExists() {
        Mockito.when(amazonS3.doesObjectExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(amazonS3.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(new S3Object());
        assertNotNull(amazonS3Service.download("user", "fileName"));
    }

    @Test
    public void testUpload() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", "application/json");
        Mockito.when(amazonS3.putObject(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(InputStream.class), Mockito.any(ObjectMetadata.class))).thenReturn(new PutObjectResult());
        assertNotNull(amazonS3Service.upload("user", Optional.of(metadata),
                new ByteArrayInputStream("Test".getBytes())));
    }
}
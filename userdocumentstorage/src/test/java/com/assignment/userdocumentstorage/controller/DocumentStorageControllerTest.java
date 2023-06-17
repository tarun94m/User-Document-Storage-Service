package com.assignment.userdocumentstorage.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.assignment.userdocumentstorage.service.AmazonS3Service;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentStorageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new DocumentStorageController(amazonS3Service)).build();
    }

    @Test
    public void testSearchAPI() throws Exception {
        Mockito.when(amazonS3Service.search(Mockito.anyString())).thenReturn(List.of("user1/logistics"));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/search/user1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testDownloadAPIWhenObjectExists() throws Exception {
        S3Object obj = new S3Object();
        obj.setObjectContent(new ByteArrayInputStream("Test".getBytes()));
        ObjectMetadata objectMetadata=new ObjectMetadata();
        objectMetadata.setContentType("application/octet-stream");
        obj.setObjectMetadata(objectMetadata);
        Mockito.when(amazonS3Service.download(Mockito.anyString(), Mockito.anyString())).thenReturn(obj);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/download/user/logistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testDownloadAPIWhenObjectDoesNotExists() throws Exception {
        Mockito.when(amazonS3Service.download(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/download/user/logistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUploadAPI() throws Exception {
        Mockito.when(amazonS3Service.upload(Mockito.anyString(), Mockito.any(Optional.class), Mockito.any(InputStream.class)))
                .thenReturn(new PutObjectResult());
        File f = new File("dummy.txt");
        f.createNewFile();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/upload/")
                        .param("filePath", "dummy.txt")
                        .param("user", "user1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        f.delete();
    }
}
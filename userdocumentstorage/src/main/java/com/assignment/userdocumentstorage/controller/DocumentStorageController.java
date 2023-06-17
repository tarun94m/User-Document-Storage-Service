package com.assignment.userdocumentstorage.controller;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.assignment.userdocumentstorage.service.AmazonS3Service;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
public class DocumentStorageController {
    private AmazonS3Service amazonS3Service;

    public DocumentStorageController(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    @GetMapping("/search/{userName}")
    public List<String> search(@PathVariable String userName) {
        return amazonS3Service.search(userName);
    }

    @PostMapping("upload")
    public PutObjectResult upload(@RequestParam("filePath") String filePath,
                                  @RequestParam("user") String user) throws IOException {
        Path path = Paths.get(filePath);
        String fileName = user + "/" + path.getFileName().toString();
        return amazonS3Service.upload(fileName, Optional.empty(), Files.newInputStream(path));
    }

    @GetMapping("/download/{userName}/{fileName}")
    @ResponseBody
    public ResponseEntity<?> download(@PathVariable String userName,
                                      @PathVariable String fileName) throws IOException {
        S3Object s3Object = amazonS3Service.download(userName, fileName);
        if (null != s3Object) {
            String contentType = s3Object.getObjectMetadata().getContentType();
            var bytes = s3Object.getObjectContent().readAllBytes();

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.valueOf(contentType));
            header.setContentLength(bytes.length);
            header.setContentDisposition(ContentDisposition.attachment().build());
            return ResponseEntity.ok()
                    .headers(header)
                    .body(bytes);
        }
        return ResponseEntity.status(404).body("File not found");
    }
}

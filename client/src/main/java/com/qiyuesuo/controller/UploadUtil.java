package com.qiyuesuo.controller;

import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


public class UploadUtil {
    private static final Logger logger = Logger.getLogger(UploadUtil.class);
    public static File multipartFileToFile(MultipartFile file, String fileName) {
        if (file != null) {
            File conventFile = new File(fileName);
            try {
                file.transferTo(conventFile);
                return conventFile;
            } catch (IOException e) {
                e.printStackTrace();
                logger.debug(e.getMessage());
            }
        }
        return null;
    }

    public static String uploadFileTest(MultipartFile file, String url_str) {
        ResponseEntity<String> stringResponseEntity = null;
        File tempFile = null;
        try {
            HttpHeaders headers = ClientFileController.builderHeader();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setConnection("Keep-Alive");
            headers.setCacheControl("no-cache");
            String originalFilename = file.getOriginalFilename();
            File file1 = new File("/");
            tempFile = multipartFileToFile(file, file1.getAbsolutePath() + "\\" + originalFilename);
            RestTemplate restTemplate = new RestTemplate();
            FileSystemResource resource = new FileSystemResource(tempFile);
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", resource);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(formData, headers);
            stringResponseEntity = restTemplate.postForEntity(url_str, request, String.class);
            tempFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
        return stringResponseEntity.getBody();
    }
}

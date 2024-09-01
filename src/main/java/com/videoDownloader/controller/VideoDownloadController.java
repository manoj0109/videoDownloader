package com.videoDownloader.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.videoDownloader.service.VideoDownloadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
public class VideoDownloadController {

    @Autowired
    private VideoDownloadService videoDownloadService;

    @Operation(summary = "Download Video", description = "Downloads a video from the provided URL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Video downloaded successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/download")
    public ResponseEntity<?> downloadVideo(@RequestParam String url) {
        try {
            File file = videoDownloadService.downloadVideo(url);

            String fileName = file.getName();
            String sanitizedFileName = sanitizeFilename(fileName);
            

//            FileSystemResource resource = new FileSystemResource(file);
            
            
            InputStreamResource resource = null;
            resource = new InputStreamResource(new FileInputStream(file));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("file", "force-download"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+sanitizedFileName);
            headers.set("fileName", sanitizedFileName);
            System.out.println("file name : "+ sanitizedFileName);
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sanitizedFileName + "\"");

//            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace to console
            return new ResponseEntity<>("Faid do download file !!",HttpStatus.BAD_REQUEST);
        }
    }

    // Method to sanitize filename
    private String sanitizeFilename(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~")
                .replace("%","");
    }	
}

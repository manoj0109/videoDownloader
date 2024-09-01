package com.videoDownloader.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class VideoDownloadService {

    @Value("${yt-dlp.path}")
    private String ytDlpPath;

    @Value("${download.dir}")
    private String downloadDir;

    public File downloadVideo(String url) {
        try {
            System.out.println("URL: " + url);

            // Create the download directory if it does not exist
            File dir = new File(downloadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Get yt-dlp_x86.exe from resources and make it executable
            File ytDlpExecutable = new File("yt-dlp_x86.exe");
            if (!ytDlpExecutable.exists()) {
                System.out.println("yt-dlp_x86.exe not found. Copying from resources...");
                try (InputStream is = new ClassPathResource("yt-dlp_x86.exe").getInputStream()) {
                    Path tempFile = Files.createTempFile("yt-dlp_x86", ".exe");
                    Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    ytDlpExecutable = tempFile.toFile();
                    ytDlpExecutable.setExecutable(true);
                    System.out.println("yt-dlp_x86.exe copied to: " + ytDlpExecutable.getAbsolutePath());
                }
            } else {
                System.out.println("yt-dlp_x86.exe found at: " + ytDlpExecutable.getAbsolutePath());
            }

            // Construct the command to execute yt-dlp
            List<String> command = new ArrayList<>();
            command.add(ytDlpExecutable.getAbsolutePath());
            command.add("-f");
            command.add("bestvideo+bestaudio/best");
            command.add("--merge-output-format");
            command.add("mp4");
            command.add("-o");
            command.add(downloadDir + "/%(title)s.%(ext)s");
            command.add(url);

            System.out.println("Executing command: " + String.join(" ", command));
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Capture and print output and error streams
            try (Scanner outputScanner = new Scanner(process.getInputStream())) {
                while (outputScanner.hasNextLine()) {
                    System.out.println("Output: " + outputScanner.nextLine());
                }
            }
            try (Scanner errorScanner = new Scanner(process.getErrorStream())) {
                while (errorScanner.hasNextLine()) {
                    System.err.println("Error: " + errorScanner.nextLine());
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("yt-dlp process failed with exit code " + exitCode);
            }

            // Find the downloaded file
            File[] files = dir.listFiles((d, name) -> name.endsWith(".mp4"));
            if (files == null || files.length == 0) {
                throw new IOException("No video file found after download.");
            }

            // Return the first file found
            return files[0];

        } catch (Exception e) {
            e.printStackTrace(); // Prints the stack trace to the console
            return null;
        }
    }
}

package org.example.fileHandling.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService implements FileServiceInterface {
    @Override
    public String uploadImage(String path, MultipartFile imageFile) throws IOException {
        String originalFilename = imageFile.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();

        if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
            throw new IllegalArgumentException("Invalid file name or file without extension");
        }

        String generatedFilename = uuid.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));

        File destinationDirectory = new File(path);
        if (!destinationDirectory.exists()) {
            boolean created = destinationDirectory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create the directory: " + path);
            }
            System.out.println("Directory created at: " + destinationDirectory.getAbsolutePath());
        }
        Path destinationFilePath = Paths.get(path, generatedFilename);

        try(InputStream inputStream = imageFile.getInputStream()) {
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return generatedFilename;
    }
}

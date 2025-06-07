package org.example.fileHandling.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileServiceInterface {
    String uploadImage(String path, MultipartFile image) throws IOException;
}

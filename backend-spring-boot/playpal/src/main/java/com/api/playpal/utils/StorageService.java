package com.api.playpal.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    private static final String IMAGE_DIR = "images";

    public String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) return null;
        String extension = Optional.ofNullable(image.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".jpg");
        Set<String> allowedExtensions = Set.of(".png", ".jpg", ".jpeg");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Extension not allowed: " + extension);
        }
        String filename = UUID.randomUUID() + extension;
        Path imagePath = Paths.get(IMAGE_DIR, filename);

        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, image.getBytes());

        return "/images/" + filename;
    }

    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return false;

        try {
            // Extraer solo el nombre del archivo
            Path path = Paths.get("images").resolve(Paths.get(imagePath).getFileName());
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

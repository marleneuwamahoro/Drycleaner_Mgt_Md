package com.drycleaner.Service;  // Ensure this matches your intended package

import com.drycleaner.Model.FileModel;  // Update this if needed
import com.drycleaner.repository.FileRepository; // Update this if needed
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final String uploadDir = "uploads/"; // Use a relative path

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;

        // Create the upload directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public FileModel uploadFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("File name is empty or invalid");
        }

        String fileType = file.getContentType();
        String fileUrl = uploadDir + fileName;

        Path path = Paths.get(fileUrl);
        // Check if file already exists
        if (Files.exists(path)) {
            throw new IOException("File already exists: " + fileName);
        }

        // Copy the file to the specified path
        Files.copy(file.getInputStream(), path);

        // Create a new FileModel instance and set its properties
        FileModel fileModel = new FileModel();
        fileModel.setResumeFileName(fileName);
        fileModel.setResumeFileType(fileType);
        fileModel.setResumeFileUrl(fileUrl);

        return fileRepository.save(fileModel); // Save the FileModel to the database
    }

    public FileModel getFile(Long id) {
        // Fetch the FileModel by its ID
        return fileRepository.findById(id).orElse(null);
    }

    public List<FileModel> getAllFiles() {
        // Fetch all FileModels from the database
        return fileRepository.findAll();
    }
}

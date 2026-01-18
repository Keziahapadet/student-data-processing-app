package com.compulynx.studentdata.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FilePathService {

    @Value("${app.output-path.linux}")
    private String linuxPath;

    @Value("${app.output-path.windows}")
    private String windowsPath;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public Path resolveDirectory() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String base = os.contains("win") ? windowsPath : linuxPath;
        Path dir = Paths.get(base);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    public Path buildFilePath(String prefix, String extension) throws IOException {
        Path directory = resolveDirectory();
        String filename = prefix + "-" + LocalDateTime.now().format(FORMATTER) + extension;
        return directory.resolve(filename);
    }
}

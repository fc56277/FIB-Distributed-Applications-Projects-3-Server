package com.mycompany.components.utils.image;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;

public class ImageFileUtils {

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static File getFile(String filename) throws IllegalStateException, IllegalArgumentException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource("images");
        if (url == null) {
            throw new IllegalStateException("Static folder 'images' does not exist");
        }
        String path = url.getPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IllegalStateException("Files in directory were empty");
        }
        return Arrays.stream(files)
                .filter(file -> file.getName().equals(filename))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File does not exist"));
    }

    public static String getBase64File(String filename) throws IOException {
        File file = ImageFileUtils.getFile(filename);
        return ImageFileUtils.getBase64File(file);
    }

    public static String getBase64File(Part filePart) throws IOException {
        byte[] bytes = IOUtils.toByteArray(filePart.getInputStream());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String getBase64File(File file) throws IOException {
        byte[] bytes = IOUtils.toByteArray(Files.newInputStream(file.toPath()));
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String getBase64File(InputStream stream) throws IOException {
        byte[] bytes = IOUtils.toByteArray(stream);
        return Base64.getEncoder().encodeToString(bytes);
    }


}

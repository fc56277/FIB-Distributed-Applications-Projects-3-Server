package com.mycompany.components.serverModels;

import com.mycompany.components.utils.image.ImageFileUtils;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Image {

    private int id;

    // TITLE, DESCRIPTION, KEYWORDS, AUTHOR, CREATOR, CAPTURE_DATE, STORAGE_DATE, FILENAME
    private String title;
    private String description;
    private List<String> keywords;
    private String author;
    private String creator;
    private LocalDate captureDate;
    private LocalDate storageDate;
    private String base64;

    public Image(String title, String description, List<String> keywords,
                 String author, String creator, LocalDate captureDate,
                 LocalDate storageDate, String base64) {
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.author = author;
        this.creator = creator;
        this.captureDate = captureDate;
        this.storageDate = storageDate;
        this.base64 = base64;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDate getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(LocalDate captureDate) {
        this.captureDate = captureDate;
    }

    public LocalDate getStorageDate() {
        return storageDate;
    }

    public void setStorageDate(LocalDate storageDate) {
        this.storageDate = storageDate;
    }

    public String getBase64() {
        return this.base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public static Image newInstance(String title, String description,
                                    List<String> keywords, String author, String creator, LocalDate captureDate,
                                    LocalDate storageDate, String base64) {
        return new Image(title,
                description,
                keywords,
                author,
                creator,
                captureDate,
                storageDate,
                base64);
    }

    public static Image fromResultSet(ResultSet rs) throws SQLException, ParseException {
        List<String> keywords = Arrays
                .asList(rs.getString("keywords").split("\\s*,\\s*"));
        LocalDate captureDate = LocalDate.parse(rs.getString("capture_date"), ImageFileUtils.dateFormatter);
        LocalDate storageDate = LocalDate.parse(rs.getString("storage_date"), ImageFileUtils.dateFormatter);
        Blob blob = rs.getBlob("base64");
        byte[] blobData = blob.getBytes(1, (int) blob.length());
        String base64 = new String(blobData);
        Image result = Image.newInstance(
                rs.getString("title"),
                rs.getString("description"),
                keywords,
                rs.getString("author"),
                rs.getString("creator"),
                captureDate,
                storageDate,
                base64
        );
        result.setId(rs.getInt("id"));
        return result;
    }
}

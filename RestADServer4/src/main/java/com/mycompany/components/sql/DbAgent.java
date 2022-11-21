/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.components.sql;

import com.mycompany.components.serverModels.Image;
import com.mycompany.components.serverModels.User;
import com.mycompany.components.utils.StringSimilarity;
import com.mycompany.components.utils.image.ImageFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author alumne
 */
public class DbAgent {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final DbConnector connector = new DbConnector();

    public void resetTables() {
        dropImageTable();
        dropUserTable();
        createTables();
    }

    private void dropUserTable() {
        String query;
        PreparedStatement ps = null;
        try {
            connector.open();
            logger.info("Connector is open (dropExistingTables): " + connector.isValid(10));
            query = "drop table users";
            ps = connector.prepareStatement(query);
            connector.executeUpdate(ps);
        } catch (SQLException e) {
            logger.error("Dropping user-table failed: ", e);
        } finally {
            connector.closePreparedStatement(ps);
            connector.closeConnection();
        }
    }

    private void dropImageTable() {
        String query;
        PreparedStatement ps = null;
        try {
            connector.open();
            logger.info("Connector is open (dropExistingTables): " + connector.isValid(10));
            query = "drop table images";
            ps = connector.prepareStatement(query);
            connector.executeUpdate(ps);
        } catch (Exception e) {
            logger.error("Dropping image-table failed: ", e);
        } finally {
            connector.closePreparedStatement(ps);
            connector.closeConnection();
        }
    }

    private void createTables() {
        String query;
        PreparedStatement ps = null;
        try {
            connector.open();
            logger.info("Connector is open (createTables): " + connector.isValid(10));
            query = "create table users (username varchar (256) primary key, password varchar (256))";
            ps = connector.prepareStatement(query);
            connector.executeUpdate(ps);
            query = "create table images (id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "title varchar (256) NOT NULL, description varchar (1024) NOT NULL, keywords "
                    + "varchar (256) NOT NULL, author varchar (256) NOT NULL, creator varchar (256) NOT NULL, "
                    + "capture_date varchar (10) NOT NULL, storage_date varchar (10) NOT NULL, "
                    + "base64 blob (2147483646) NOT NULL, primary key (id), foreign key (creator) references users(username))";
            ps = connector.prepareStatement(query);
            connector.executeUpdate(ps);
        } catch (SQLException e) {
            logger.error("Creating SQL tables failed.", e);
        } finally {
            connector.closePreparedStatement(ps);
        }
    }

    public void insertUser(User user) {
        String query;
        PreparedStatement ps = null;
        try {
            connector.open();
            logger.info("Connector is open (insertUser): " + connector.isValid(10));
            query = "insert into users values(?,?)";
            ps = connector.prepareStatement(query);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getHashedPassword());
            connector.executeUpdate(ps);
        } catch (SQLException e) {
            logger.error("Inserting user to database failed.", e);
        } finally {
            connector.closePreparedStatement(ps);
            connector.closeConnection();
        }
    }

    public boolean userExists(String username) {
        String query;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connector.open();
            logger.info("Connector is open (userExists): " + connector.isValid(10));
            query = "select * from users where username = ?";
            ps = connector.prepareStatement(query);
            ps.setString(1, username);
            rs = connector.executeQuery(ps);
            return rs.next();
        } catch (SQLException e) {
            logger.error("Checking if user exists failed.", e);
            return false;
        } finally {
            connector.closeAll(rs, ps);
        }
    }

    public User getUser(String username, String password) throws SQLException {
        String query;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connector.open();
            logger.info("Connector is open (getUser): " + connector.isValid(10));
            query = "SELECT username, password FROM users WHERE username = ? AND password = ?";
            ps = connector.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = connector.executeQuery(ps);
            if (rs.next()) {
                return User.newInstance(
                        rs.getString("username"),
                        rs.getString("password")
                );
            }
            return null;
        } finally {
            connector.closeAll(rs, ps);
        }
    }

    public void insertImage(Image image) throws SQLException {
        String query;
        PreparedStatement ps = null;
        try {
            Date captureDate = image.getCaptureDate();
            Date now = new Date();
            connector.open();
            query = "insert into images" +
                    "(title, description, keywords, author, creator, capture_date, storage_date, base64)" +
                    " values(?,?,?,?,?,?,?,?)";
            ps = connector.prepareStatement(query);
            ps.setString(1, image.getTitle());
            ps.setString(2, image.getDescription());
            ps.setString(3, String.join(",", image.getKeywords()));
            ps.setString(4, image.getAuthor());
            ps.setString(5, image.getCreator());
            ps.setString(6, ImageFileUtils.dateFormatter.format(captureDate));
            ps.setString(7, ImageFileUtils.dateFormatter.format(now));
            ps.setBlob(8, new SerialBlob(image.getBase64().getBytes()));
            connector.executeUpdate(ps);
        } finally {
            connector.closePreparedStatement(ps);
            connector.closeConnection();
        }
    }

    public void insertImage(
            String title,
            String description,
            List<String> keywords,
            String author,
            String creator,
            Date captureDate,
            Date storageDate,
            String base64) throws SQLException {
        insertImage(Image.newInstance(
                title,
                description,
                keywords,
                author,
                creator,
                captureDate,
                storageDate,
                base64
        ));
    }

    public List<Image> getAllImages() throws SQLException, ParseException {
        String query;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connector.open();
            logger.info("Connector is open (getAllImages): " + connector.isValid(10));
            query = "SELECT * FROM images";
            ps = connector.prepareStatement(query);
            rs = connector.executeQuery(ps);
            List<Image> result = new ArrayList<>();
            while (rs.next()) {
                result.add(Image.fromResultSet(rs));
            }
            return result;
        } finally {
            connector.closeAll(rs, ps);
        }
    }

    public List<Image> searchImageByTitle(String searchParam) throws SQLException, ParseException {
        /*
        query = "SELECT * FROM images where filename like ?";
        This below line, for some reason, doesn't work. Inserting percentage signs should
        be able to find similar documents, however with the Java-version in question, it
        doesn't do shit. So this is the correct solution, but it won't behave correctly.
        We therefore have to use 'like' directly with the exact name
        String formattedSearchParam = "'%" + searchParam + "%'";
        Therefore, we had to completely rework the code. Instead of SQL + like,
        we extracted all images, and manually filtered them :))
        */
        List<Image> allImages = this.getAllImages();
        List<Image> result = new ArrayList<>();
        for (Image image : allImages) {
            if (StringSimilarity.similarity(image.getTitle(), searchParam) >= 0.3) {
                result.add(image);
            }
        }
        return result;
    }

    public Image getImageById(String id) throws SQLException, ParseException {
        String query;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connector.open();
            query = "SELECT * FROM images WHERE id = ?";
            ps = connector.prepareStatement(query);
            ps.setString(1, id);
            rs = connector.executeQuery(ps);
            if (rs.next()) {
                return Image.fromResultSet(rs);
            }
            return null;
        } finally {
            connector.closeAll(rs, ps);
        }
    }

    public Image getImageByTitle(String title) throws SQLException, ParseException {
        String query;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connector.open();
            query = "SELECT * FROM images WHERE title = ?";
            ps = connector.prepareStatement(query);
            ps.setString(1, title);
            rs = connector.executeQuery(ps);
            if (rs.next()) {
                return Image.fromResultSet(rs);
            }
            return null;
        } finally {
            connector.closeAll(rs, ps);
        }
    }

    public void updateImage(String id, Image image) throws SQLException {
        // Inefficient, but easy and saves time ¯\_(ツ)_/¯
        this.deleteImageById(id);
        this.insertImage(image);
    }

    public void deleteImageById(String id) throws SQLException {
        String query;
        PreparedStatement ps = null;
        try {
            connector.open();
            query = "DELETE * FROM images WHERE id = ?";
            ps = connector.prepareStatement(query);
            ps.setString(1, id);
            connector.executeUpdate(ps);
        } catch (SQLException e) {
            logger.error("Failed to delete image.", e);
            throw e;
        } finally {
            connector.closePreparedStatement(ps);
        }
    }
}

package com.mycompany.server.resources;

import com.mycompany.components.serverModels.Image;
import com.mycompany.components.utils.image.ImageFileUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

@Path("/image")
public class ImageResource extends BaseResource {

    /**
     * POST method to register a new image
     *
     * @param headers Request headers containing auth-information
     * @param title Title of image
     * @param description Description of image
     * @param keywords Image tags
     * @param author Author of image
     * @param captureDateString Date when image was captured
     * @param base64 Base64 encoding of the image
     * @return Response with either success, or indicated error
     */
    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerImage(@Context HttpHeaders headers,
                                  @FormParam("title") String title,
                                  @FormParam("description") String description,
                                  @FormParam("keywords") String keywords,
                                  @FormParam("author") String author,
                                  @FormParam("capture") String captureDateString,
                                  @FormParam("file") String base64) {
        logger.info("Calling register image.");
        if (!isAuthorized(headers)) {
            logger.info("User is unauthorized - returning redirect");
            return this.redirect();
        }
        try {
            logger.info("User authorized. Starting parsing of data.");
            String creator = new String(Base64.getDecoder().decode(headers.getHeaderString("username")));
            LocalDate captureDate = LocalDate.parse(captureDateString, ImageFileUtils.dateFormatter);
            LocalDate storageDate = LocalDate.now();
            dbAgent.insertImage(
                    title, description,
                    Arrays.asList(keywords.split("\\s*,\\s*")),
                    author, creator, captureDate,
                    storageDate,
                    base64);
            logger.info("Image registration successful");
            return this.success(Response.Status.CREATED, "Succesfully registered image.");
        } catch (SQLException e) {
            logger.error("SQL error thrown in registerImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * POST method to modify an existing image
     *
     * @param id ID of image to modify
     * @param title Title of image
     * @param description Description of image
     * @param keywords Image tags
     * @param author Author of image
     * @param captureDateString Date when image was captured
     * @return Response with either success, or indicated error
     */
    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateImage(@Context HttpHeaders headers,
                                @FormParam("id") String id,
                                @FormParam("title") String title,
                                @FormParam("description") String description,
                                @FormParam("keywords") String keywords,
                                @FormParam("author") String author,
                                @FormParam("capture") String captureDateString,
                                @FormParam("file") String base64) {
        logger.info("Calling update image.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        try {
            logger.info("Authorization successful. Starting parsing");
            Image image = dbAgent.getImageById(Integer.parseInt(id));
            if (image == null) {
                throw new BadRequestException("No image found with given ID");
            }
            if (!Objects.equals(title, "null")) {
                image.setTitle(title);
            }
            if (!Objects.equals(description, "null")) {
                image.setDescription(description);
            }
            if (!Objects.equals(keywords, "null")) {
                List<String> keywordsList = Arrays.asList(keywords.split("\\s*,\\s*"));
                image.setKeywords(keywordsList);
            }
            if (!Objects.equals(author, "null")) {
                image.setAuthor(author);
            }
            if (!Objects.equals(captureDateString, "null")) {
                LocalDate captureDate = LocalDate.parse(captureDateString, ImageFileUtils.dateFormatter);
                image.setCaptureDate(captureDate);
            }
            if (!Objects.equals(base64, "null")) {
                image.setBase64(base64);
            }
            dbAgent.updateImage(Integer.parseInt(id), image);
            logger.info("Image update successful");
            return this.success(Response.Status.OK, "Succesfully updated image.");
        } catch (BadRequestException e) {
            logger.error("Bad request error thrown in updateImage");
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in updateImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in updateImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * POST method to delete an existing image
     *
     * @param id ID of image to delete
     * @return Response with either success, or indicated error
     */
    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteImage(@Context HttpHeaders headers,
                                @FormParam("id") String id) {
        logger.info("Calling delete image.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Login successful. Starting deletion");
        try {
            String creator = new String(Base64.getDecoder().decode(headers.getHeaderString("username")));
            Image image = dbAgent.getImageById(Integer.parseInt(id));
            if (!Objects.equals(image.getCreator(), creator)) {
                logger.info("Denying deletion - user unauthorized.");
                throw new NotAuthorizedException("User is not creator of picture.");
            }
            dbAgent.deleteImageById(Integer.parseInt(id));
            return success(Response.Status.OK, "Successfully deleted image.");
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in deleteImage");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in deleteImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in deleteImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * GET method to list images
     *
     * @return Response with either success, or indicated error
     */
    @GET
    @Path("list")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listImages(@Context HttpHeaders headers) {
        logger.info("Calling list images.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Authorization successful. Starting parsing");
        try {
            List<Image> images = dbAgent.getAllImages();
            return success(Response.Status.OK, images);
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in deleteImage");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in deleteImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in deleteImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * GET method to search images by id
     *
     * @param id ID of image to search for
     * @return
     */
    @GET
    @Path("searchID/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByID(@Context HttpHeaders headers, @PathParam("id") String id) {
        logger.info("Calling search by ID.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Authorization successful. Starting parsing");
        try {
            List<Image> images = dbAgent.searchImageById(Integer.parseInt(id));
            return success(Response.Status.OK, images);
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in deleteImage");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in deleteImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in deleteImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * GET method to search images by title
     *
     * @param title Title of image to search for
     * @return
     */
    @GET
    @Path("searchTitle/{title}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByTitle(@Context HttpHeaders headers, @PathParam("title") String title) {
        logger.info("Calling search by title.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Authorization successful. Starting parsing");
        try {
            List<Image> images = dbAgent.searchImageByTitle(title);
            return this.success(Response.Status.OK, images);
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in searchTitle");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in searchTitle", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in searchTitle", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * GET method to search images by creation date. Date format should be
     * yyyy-mm-dd
     *
     * @param date Date of image to search for
     * @return
     */
    @GET
    @Path("searchCreationDate/{date}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByCreationDate(@Context HttpHeaders headers, @PathParam("date") String date) {
        logger.info("Calling search by creation date.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Authorization successful. Starting parsing");
        try {
            LocalDate searchDate = LocalDate.parse(date, ImageFileUtils.dateFormatter);
            List<Image> images = dbAgent.searchImageByCreationDate(searchDate);
            return success(Response.Status.OK, images);
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in deleteImage");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in deleteImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in deleteImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * GET method to search images by author
     *
     * @param author Author of image to search for
     * @return
     */
    @GET
    @Path("searchAuthor/{author}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByAuthor(@Context HttpHeaders headers, @PathParam("author") String author) {
        logger.info("Calling search by author.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Authorization successful. Starting parsing");
        try {
            List<Image> images = dbAgent.searchByAuthor(author);
            return success(Response.Status.OK, images);
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in deleteImage");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in deleteImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in deleteImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * GET method to search images by keyword
     *
     * @param keywords Keywords of image to search for
     * @return
     */
    @GET
    @Path("searchKeywords/{keywords}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByKeywords(@Context HttpHeaders headers, @PathParam("keywords") String keywords) {
        logger.info("Calling search by keywords.");
        if (!isAuthorized(headers)) {
            return this.redirect();
        }
        logger.info("Authorization successful. Starting parsing");
        try {
            List<String> keywordsList = Arrays.asList(keywords.split("\\s*,\\s*"));
            List<Image> images = dbAgent.searchByKeywords(keywordsList);
            return success(Response.Status.OK, images);
        } catch (NotAuthorizedException e) {
            logger.error("Not authorized error thrown in deleteImage");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (ParseException e) {
            logger.error("Parse error thrown in deleteImage", e);
            return this.error(Response.Status.BAD_REQUEST, e);
        } catch (SQLException e) {
            logger.error("SQL error thrown in deleteImage", e);
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }
}

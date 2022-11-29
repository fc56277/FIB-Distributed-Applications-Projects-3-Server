package com.mycompany.server.resources;

import com.mycompany.components.serverModels.Image;
import com.mycompany.components.serverModels.User;
import com.mycompany.components.utils.image.ImageFileUtils;
import com.mycompany.components.utils.user.PasswordHasher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

@Path("/admin")
public class AdminResource extends BaseResource {

    private final PasswordHasher hasher = new PasswordHasher();

    @GET
    @Path("init")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetDatabase(@QueryParam("pass") String password) {
        if (Objects.equals(password, "supersecretpassword123")) {
            this.dbAgent.resetTables();
            return success(Response.Status.OK, "Successfully reset database.");
        }
        return error(Response.Status.FORBIDDEN, new NotAuthorizedException("Wrong password"));
    }

    @GET
    @Path("addImages")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDefaultImage(@QueryParam("pass") String password) {
        if (Objects.equals(password, "supersecretpassword123")) {
            logger.info("Admin password correct. Starting loading local images.");
            try {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                URL url = classloader.getResource("images");
                assert url != null;
                String path = url.getPath();
                logger.info("Created enumeration");
                for (File file : Objects.requireNonNull(new File(path).listFiles())) {
                    logger.info("First file extracted with name: " + file.getName(), file);
                    Image image = Image.newInstance(
                            file.getName(),
                            "temp",
                            Collections.singletonList("sup"),
                            "admin",
                            "admin",
                            LocalDate.now(),
                            LocalDate.now(),
                            password);
                    String base64 = ImageFileUtils.getBase64File(file);
                    image.setBase64(base64);
                    logger.info("Successfully created the image.");
                    dbAgent.insertImage(image);
                    logger.info("Successfully inserted image!");
                }
                return success(Response.Status.OK, "Successfully added example images.");
            } catch (IOException | SQLException e) {
                logger.error("Failed to insert images as admin.");
                return error(Response.Status.INTERNAL_SERVER_ERROR, e);
            }
        }
        return error(Response.Status.FORBIDDEN, new NotAuthorizedException("Wrong password"));
    }

    @GET
    @Path("addAdmin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDefaultUser(@QueryParam("pass") String password) {
        if (Objects.equals(password, "supersecretpassword123")) {
            logger.info("Admin password correct. Adding default user.");
            dbAgent.insertUser(User.newInstance("admin", hasher.generatePasswordHash("password")));
            return success(Response.Status.OK, "Successfully added default user.");
        }
        return error(Response.Status.FORBIDDEN, new NotAuthorizedException("Wrong password"));
    }

}

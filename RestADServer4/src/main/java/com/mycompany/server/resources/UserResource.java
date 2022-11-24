package com.mycompany.server.resources;

import com.mycompany.components.serverModels.User;
import com.mycompany.components.utils.user.PasswordHasher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;

@Path("/user")
public class UserResource extends BaseResource {

    private final PasswordHasher hasher = new PasswordHasher();

    /**
     * POST method to login in the application
     *
     * @param username Username of user trying to log in
     * @param password Password in plaintext
     * @return Response indicating error or success
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@FormParam("username") String username,
                              @FormParam("password") String password) {
        System.out.println("USERNAME VALUE: " + username);
        System.out.println("PASSWORD VALUE: " + password);
        logger.info("Attempting to login with username: " + username);
        try {
            if (!this.userVerifier.isAuthorized(username, password)) {
                logger.info("User login unsuccessful");
                throw new NotAuthorizedException("User does not exist");
            }
            String encodedUsername = Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8));
            logger.info("User login successful");
            return this.success(
                    Response.Status.OK,
                    "Login successful",
                    Collections.singletonMap("username", encodedUsername)
            );
        } catch (SQLException e) {
            logger.error("SQL Error thrown in user-login");
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        } catch (NotAuthorizedException e) {
            logger.error("Not-authorized-error thrown in user-login");
            return this.error(Response.Status.FORBIDDEN, e);
        } catch (Exception e) {
            logger.error("Generic exception occurred");
            return this.error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * POST method to register in the application
     *
     * @param username Username of user trying to register
     * @param password Password in plaintext
     * @return Response indicating error or success
     */
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@FormParam("username") String username,
                                 @FormParam("password") String password) {
        logger.info("Registering user");
        try {
            dbAgent.insertUser(User.newInstance(username, hasher.generatePasswordHash(password)));
            return this.success(Response.Status.CREATED, "User registration successful.");
        } catch (NotAuthorizedException e) {
            logger.error("Not-authorized-error thrown in user-login.", e);
            return this.error(Response.Status.FORBIDDEN, e);
        }
    }
}

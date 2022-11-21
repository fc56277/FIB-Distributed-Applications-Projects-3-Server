package com.mycompany.server.resources;

import com.google.gson.Gson;
import com.mycompany.components.sql.DbAgent;
import com.mycompany.components.utils.user.UserVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class BaseResource {

    protected final Gson gson = new Gson();
    protected final DbAgent dbAgent = new DbAgent();
    protected final UserVerifier userVerifier = new UserVerifier();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected boolean isUnauthorized(HttpHeaders httpHeaders) {
        // If we want to get extra points - this verification should be more complicated (JWT)
        if (!httpHeaders.getRequestHeaders().containsKey("username")) {
            return true;
        }
        String encodedUsername = httpHeaders.getHeaderString("username");
        String username = new String(Base64.getDecoder().decode(encodedUsername));
        return dbAgent.userExists(username);
    }

    protected String toJsonString(String key, String message) {
        return this.toJsonString(Collections.singletonMap(key, message));
    }

    protected String toJsonString(Object o) {
        return gson.toJson(o);
    }

    protected Response success(Response.Status status, String message) {
        return buildResponse(status, "message", message).build();
    }

    protected Response success(Response.Status status, Object response) {
        return buildResponse(status, response).build();
    }

    protected Response success(Response.Status status, Object response, Map<String, String> headerValues) {
        return buildResponse(status, response, headerValues).build();
    }

    protected Response redirect() {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(toJsonString("message", "User is not authorized"))
                .location(URI.create("/"))
                .build();
    }

    protected Response error(Response.Status status, Exception e) {
        return buildResponse(status, "error", e.getMessage()).build();
    }

    private Response.ResponseBuilder buildResponse(Response.Status status, String key, String message) {
        return Response.status(status).entity(toJsonString(key, message));
    }

    private Response.ResponseBuilder buildResponse(Response.Status status, Object response) {
        return Response.status(status).entity(toJsonString(response));
    }

    private Response.ResponseBuilder buildResponse(Response.Status status, Object response, Map<String, String> headers) {
        Response.ResponseBuilder builder = Response.status(status).entity(toJsonString(response));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }
        return builder;
    }

}

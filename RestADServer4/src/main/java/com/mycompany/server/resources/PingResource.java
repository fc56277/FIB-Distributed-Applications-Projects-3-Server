package com.mycompany.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author
 */
@Path("/ping")
public class PingResource extends BaseResource {

    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response
                .ok(toJsonString("message", "Hello from ping!"))
                .build();
    }

}

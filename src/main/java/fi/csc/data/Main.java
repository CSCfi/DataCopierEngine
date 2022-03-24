package fi.csc.data;

import io.agroal.api.AgroalDataSource;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/v1/run/")
public class Main {


    @Inject
    Logger log;

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    AgroalDataSource write; //DataSource


    @GET
     @Path("{id}")
    public Response aja(@PathParam("id") String id) {

        Integer copyid;
        try {
            copyid = Integer.parseInt(id);
        } catch (NumberFormatException e) {
           return Response.status(400, "int id missing or error: " + e.getMessage()).build();
        }
        Engine e = new Engine(copyid, log, defaultDataSource, write);
        e.run();

        return Response.ok("Pyyntö lähetetty\n").build();
    }
}
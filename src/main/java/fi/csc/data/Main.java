package fi.csc.data;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/v1/run/")
public class Main {

    public final static int AD = 403; //Forbidden
    private final static String KEYERROR = "API key was INVALID";
    public final static Response ACCESSDENIED = Response.status(AD, KEYERROR).build();

    @ConfigProperty(name = "dcengine.apikey")
    String apikey;


    @Inject
    Logger log;

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    @DataSource("write")
    AgroalDataSource write;


    @GET
    @Path("{id}")
    public Response aja(@PathParam("id") String id,@HeaderParam("Apikey") String apikeytocheck) {
        /*if (!apikey.equals(apikeytocheck)) {
            log.error("Invalid Apikey: "+ apikeytocheck);
            return ACCESSDENIED;
        }*/

        int copyid;
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
package fi.csc.data;

import fi.csc.data.model.ExchangeObject;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import io.quarkus.mailer.Mailer;

@Path("/v1/run/")
public class Main {

    public final static int AD = 403; //Forbidden
    private final static String KEYERROR = "API key was INVALID";
    public final static Response ACCESSDENIED = Response.status(AD, KEYERROR).build();

    @ConfigProperty(name = "dcengine.apikey")
    String apikey;
    @ConfigProperty(name = "ldap.key")
    String ldapkey;

    @Inject
    Logger log;

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    @DataSource("write")
    AgroalDataSource write;

    @Inject Mailer mailer;

    @GET
    @Path("{id}")
    public Response aja(@PathParam("id") String id,@HeaderParam("Apikey") String apikeytocheck) {
        if (!apikey.equals(apikeytocheck)) {
            log.error("Invalid Apikey: "+ apikeytocheck);
            return ACCESSDENIED;
        }

        int copyid;
        try {
            copyid = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return Response.status(400, "int id missing or error: " + e.getMessage()).build();
        }
        long alku = System.nanoTime();
        ExchangeObject eo = new ExchangeObject();
        Engine e = new Engine(copyid, log, defaultDataSource, write, eo, mailer);
        log.info("Create two objects: " + (System.nanoTime() -alku));
        Thread t1 = new Thread(e);
        t1.start();
        // Tämä siis ajetaan rinnakkain edellä käynnistetyn säikeeen kanssa!!!
        LDAP ldap = new LDAP(ldapkey);
        log.info("Before sync: " + (System.nanoTime() - alku));
        eo.setSähköposti(ldap.emailquery(eo.getTunnus())); //Tässä synkronoidaan
        Thread t2 = new Thread(eo);
        t2.start(); //säikeet jäävät seurustelemaan, mutta tämä pääohjelma kuittaa ja poistuu
        log.info("After sync: " + (System.nanoTime() - alku));
        return Response.ok("Pyyntö lähetetty\n").build();
    }
}
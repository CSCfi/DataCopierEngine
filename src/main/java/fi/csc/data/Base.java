package fi.csc.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database read
 */
public class Base {

    /*final static String SELECT = "SELECT source.PalveluID, source.Protokolla, source.omistaja, " +
            "source.polku, auths.username, aauths.accessKey, auths.secretKey, auths.projectID, auths.token, " +
            "destination.PalveluID, destination.Protokolla, destination.omistaja, " +
            "destination.polku, ad.username, ad.accessKey, ad.secretKey, ad.projectID, ad.token" +
            "FROM request r, palvelu source, palvelu destination, auth auths, auth ad " +
            "WHERE r.source = source.caseid AND r.destination = destination.caseid AND" +
            "source.Auth = auths.authid AND destination.Auth = ad.authid";*/

    final static String SELECT = "SELECT source.PalveluID, source.Protokolla, source.omistaja, source.polku, auths.username, auths.accessKey, auths.secretKey, auths.projectID, auths.token, destination.PalveluID, destination.Protokolla, destination.omistaja,  destination.polku, ad.username, ad.accessKey, ad.secretKey, ad.projectID, ad.token FROM request r, palvelu source, palvelu destination, auth auths, auth ad WHERE r.source = source.caseid AND r.destination = destination.caseid AND source.Auth = auths.authid AND destination.Auth = ad.authid";

    static ResultSet read(Connection con) {
        try {
            PreparedStatement statement = con.prepareStatement(SELECT);
            ResultSet rs = statement.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

package fi.csc.data;

import fi.csc.data.model.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database read and write
 */
public class Base {

    final static String SELECT = "SELECT source.PalveluID, source.Protokolla, source.omistaja, source.polku," +
            " auths.username, auths.accessKey, auths.secretKey, auths.projectID, auths.token," +
            " destination.PalveluID, destination.Protokolla, destination.omistaja,  destination.polku," +
            " ad.username, ad.accessKey, ad.secretKey, ad.projectID, ad.token, r.copyid " +
            "FROM request r, palvelu source, palvelu destination, auth auths, auth ad " +
            "WHERE r.source = source.caseid AND r.destination = destination.caseid AND " +
            "source.Auth = auths.authid AND destination.Auth = ad.authid and r.status IS NULL";

    final static String UPDATE = "UPDATE request set status=?, MB=?, wallclock=? WHERE copyid=?";
    final static String START  = "UPDATE request set status=? WHERE copyid=?";

    /**
     * Lukee tietokannasta aloittamattomat työt
     *
     * @param con Connection to use
     * @return ResultSet 19 values data from database
     */
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

    /**
     * Kirjoitta tietokantaan rclone komennon lopputuloksen
     *
     * @param c2 Connection to use (different than read!)
     * @param s Status rclone results
     * @param copyid int käsitellyn tietokantarivin tunnus, jolle siis kirjoitetaan
     * @return int lines to write or negative error
     */
    public static int write(Connection c2, Status s, int copyid) {
        if (null != s) {
            try {
                PreparedStatement statement = c2.prepareStatement(UPDATE);
                statement.setInt(1, s.exitCode);
                statement.setInt(2, s.MB );
                statement.setDouble(3,s.kesto); //wallclock
                statement.setInt(4, copyid);
                int tulos = statement.executeUpdate();
                statement.close();
                c2.close();
                return tulos;
            }
             catch(SQLException e){
                    e.printStackTrace();
                    return -2;
                }
        } else return -1;
    }

    /**
     * Muutetaan käsiteltävän tietokantrivin statukseksi 11, jotta toinen pod/prosessi ei jättää sen rauhaan
     *
     * @param c2 Connection to use, same as write
     * @param copyid int käsitellyn tietokantarivin tunnus, jolle siis kirjoitetaan
     * @return int lines to write or negative error
     */
    public static int start(Connection c2, int copyid) {
         try {
                PreparedStatement statement = c2.prepareStatement(UPDATE);
                statement.setInt(1, 11); // status running
                statement.setInt(2, copyid);
                int tulos = statement.executeUpdate();
                statement.close();
                return tulos;
            }
             catch(SQLException e){
                    e.printStackTrace();
                    return -2;
                }
    }
}

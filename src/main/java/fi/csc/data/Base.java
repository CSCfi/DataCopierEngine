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
            " ad.username, ad.accessKey, ad.secretKey, ad.projectID, ad.token, r.copyid, r.requester, auths.authid, ad.authid " +
            "FROM request r, palvelu source, palvelu destination, auth auths, auth ad " +
            "WHERE r.source = source.caseid AND r.destination = destination.caseid AND " +
            "source.Auth = auths.authid AND destination.Auth = ad.authid AND r.status IS NULL AND r.copyid=?";

    final static String WRITE = "UPDATE request set status=?, MB=?, wallclock=?, nofiles=? WHERE copyid=?";
    final static String UPDATE = "UPDATE request set MB=?, nofiles=? WHERE copyid=?";
    final static String START  = "UPDATE request set status=? WHERE copyid=?";
    final static String DELETE = "DELETE FROM auth WHERE authid=?";
    private final Connection c2;
    private final int id;

    public Base(Connection connection, int id) {
        this.c2 = connection;
        this.id =id;
    }

    /**
     * Lukee tietokannasta aloittamattomat työt
     *
     * @param con Connection to use
     * @return ResultSet 19 values data from database
     */
    ResultSet read(Connection con, int id) {
        try {
            PreparedStatement stmnt = con.prepareStatement(SELECT);
            stmnt.setInt(1, id);
           return stmnt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int update(int mb, int no) {
        try {
            PreparedStatement statement = c2.prepareStatement(UPDATE);
            statement.setInt(1, mb);
            statement.setInt(2, no);
            statement.setInt(3, id);
            int tulos = statement.executeUpdate();
            statement.close();
            return tulos;
        } catch(SQLException e){
            e.printStackTrace();
            return -2;
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
    public int write(Connection c2, Status s, int copyid) {
        if (null != s) {
            try {
                PreparedStatement statement = c2.prepareStatement(WRITE);
                statement.setInt(1, s.exitCode);
                statement.setInt(2, s.MB );
                if (s.kesto < 0)
                    statement.setNull(3, java.sql.Types.NULL);
                else
                    statement.setDouble(3,s.kesto); //wallclock
                if (-1 != s.files)
                    statement.setInt(4, s.files);
                else
                    statement.setNull(4, java.sql.Types.NULL);
                statement.setInt(5, copyid);
                int tulos = statement.executeUpdate();
                statement.close();
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
    public int start(Connection c2, int copyid) {
         try {
                PreparedStatement statement = c2.prepareStatement(START);
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

    /**
     * Kopioinnin onnistuttua poistetaan autentikaatiotiedot tietokannasta
     *
     * @param c Connection with DELETE rights
     * @param id int Line authid to delete
     * @return int number of the deleted lines
     */
    public int delete(Connection c, int id) {
        try {
            PreparedStatement stmnt = c.prepareStatement(DELETE);
            stmnt.setInt(1, id);
            int pa = stmnt.executeUpdate();
            stmnt.close();
            return pa;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

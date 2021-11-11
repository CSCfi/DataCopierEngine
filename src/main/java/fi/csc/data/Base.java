package fi.csc.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database read
 */
public class Base {

    final static String SELECT = "SELECT * FROM request";

    static /*CopyRequest*/ int read(Connection con) {
        try {
            PreparedStatement statement = con.prepareStatement(SELECT);
            ResultSet rs = statement.executeQuery();
            rs.afterLast();
            return rs.getRow();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

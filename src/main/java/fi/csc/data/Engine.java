package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import fi.csc.data.model.Status;
import io.agroal.api.AgroalDataSource;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Engine implements Runnable{
    private int id;
    Logger log;
    AgroalDataSource defaultDataSource;
    AgroalDataSource write;

    public Engine(int id, Logger log, AgroalDataSource defaultDataSource, AgroalDataSource write) {
        this.id = id;
        this.log = log;
        this.defaultDataSource = defaultDataSource;
        this.write = write;
    }

    @Override
    public void run()  {

        System.out.println("Hello " + id);

        try {
            Status s;
            Connection c2 = write.getConnection();
            Connection connection = defaultDataSource.getConnection();
            Base db = new Base();
            ResultSet rs = db.read(connection, id);
            if (null == rs) {
                log.info("Nothing to do, rs was null");
            } else {
                if (rs.first()) {
                    int copyid = rs.getInt(19);
                    System.out.println("rs.First!");
                    db.start(c2, copyid);
                    RcloneConfig source = (RcloneConfig) Const.palveluht.get(rs.getInt(1));
                    RcloneConfig destination = (RcloneConfig) Const.palveluht.get(rs.getInt(10));
                    log.info("Source: " + source.type + " Destination: " + destination.type);
                    RcloneRun rr = new RcloneRun(copyid);
                    String sourceToken = rs.getString(9);
                    String destinationToken = rs.getString(18);
                    source.access_key_id = rs.getString(6);
                    destination.access_key_id = rs.getString(15);
                    if (null == sourceToken) {
                        sourceToken = source.access_key_id;
                    }
                    rr.config(source, sourceToken);
                    if (null == destinationToken) {
                        destinationToken = destination.access_key_id;
                    }
                    s = rr.config(destination, destinationToken);
                    virhetulostus("Config: ", s.errors);
                    log.info("SourceToken: " + sourceToken + " DestinationToken: " + destinationToken);
                    source.secret_access_key = rs.getString(7);
                    destination.secret_access_key = rs.getString(16);
                    source.omistaja = rs.getString(3);
                    destination.omistaja = rs.getString(12);
                    source.polku = rs.getString(4);
                    destination.polku = rs.getString(13);
                    source.username = rs.getString(5);
                    destination.username = rs.getString(14);
                    s = rr.copy(source, destination, sourceToken, destinationToken);
                    log.info("Kesto: "+s.kesto);
                    virhetulostus("Copy: ", s.errors);
                    db.write(c2, s, copyid);
                    db.delete(c2, rs.getInt(20));
                    db.delete(c2, rs.getInt(21));
                    c2.close();
                    Statement stmt = rs.getStatement();
                    rs.close();
                    stmt.close();
                } else { // rs.first was NOT
                    log.error("There was NO rs.first()");
                }
            }
        connection.close();
    } catch (SQLException throwables) {
        throwables.printStackTrace();
    }
}

   void virhetulostus(String kohta, String errors) {
        if (null != errors && !errors.isEmpty())
            log.error(kohta + errors);
   }
}



package fi.csc.data;

import fi.csc.data.model.ExchangeObject;
import fi.csc.data.model.RcloneConfig;
import fi.csc.data.model.Status;
import io.agroal.api.AgroalDataSource;
import io.quarkus.mailer.Mail;
import org.jboss.logging.Logger;
import io.quarkus.mailer.Mailer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Engine implements Runnable{
    private int id;
    Logger log;
    AgroalDataSource defaultDataSource;
    AgroalDataSource write;
    SeurantaBean sb = new SeurantaBean();
    ExchangeObject eo;
    Mailer mailer;

    public Engine(int id, Logger log, AgroalDataSource defaultDataSource, AgroalDataSource write,
                  ExchangeObject eo, Mailer mailer) {
        this.id = id;
        this.log = log;
        this.defaultDataSource = defaultDataSource;
        this.write = write;
        this.eo = eo;
        this.mailer = mailer;
    }

    @Override
    public void run()  {

        System.out.println("Hello " + id);

        try {
            Status s;
            Connection c2 = write.getConnection();
            Connection connection = defaultDataSource.getConnection();
            Base db = new Base(c2, id);
            ResultSet rs = db.read(connection, id);
            if (null == rs) {
                log.info("Nothing to do, rs was null");
            } else {
                if (rs.first()) {
                    eo.lähetäTunnus(rs.getString(20));
                    int copyid = rs.getInt(19);
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
                    if (rr.config(source, sourceToken) < 0) {
                        log.error("Source config failed");
                    }
                    if (null == destinationToken) {
                        destinationToken = destination.access_key_id;
                    }
                    if (rr.config(destination, destinationToken) < 0)
                        log.error("Destination config failed");
                    source.secret_access_key = rs.getString(7);
                    destination.secret_access_key = rs.getString(16);
                    source.omistaja = rs.getString(3);
                    destination.omistaja = rs.getString(12);
                    source.polku = rs.getString(4);
                    destination.polku = rs.getString(13);
                    source.username = rs.getString(5);
                    destination.username = rs.getString(14);
                    Seurantasäie ss = new Seurantasäie(db, sb);
                    s = rr.copy(source, destination, sourceToken, destinationToken, ss);
                    log.info("Kesto: "+s.kesto);
                    virhetulostus("Copy: ", s.errors);
                    db.write(c2, s, copyid);
                    db.delete(c2, rs.getInt(21));
                    db.delete(c2, rs.getInt(22));
                    c2.close();
                    Statement stmt = rs.getStatement();
                    rs.close();
                    stmt.close();
                    rr.delete(source);
                    rr.delete(destination);
                    String sähköpostiosoite = eo.getEmailaddress();
                    // works only ibn rahti
                    /*mailer.send(Mail.withText(sähköpostiosoite,
                            "Tiedostokopiointisi "+source.type + " onnistui",
                    "Your file copy to " +  destination.type + "succeeded in" + s.kesto +"s.")
                            .setFrom(sähköpostiosoite));*/
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



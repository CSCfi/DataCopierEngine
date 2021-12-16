package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import io.agroal.api.AgroalDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@QuarkusMain
public class ApplicationLifecycle implements QuarkusApplication {

    @Inject
    Logger log;

    @Inject
    AgroalDataSource defaultDataSource;

    @Override
    public int run(String... args) throws Exception {

        if (args.length < 2) {
            System.out.println("Hello World!");
        } else {
            System.out.println("Hello " + args[1]);
        }
        try {
            Connection connection = defaultDataSource.getConnection();
            ResultSet rs = Base.read(connection);
            if (null == rs) {
                log.info("Nothing to do, rs was null");
            } else {
                if (rs.first()) {
                    RcloneConfig source = (RcloneConfig) Const.palveluht.get(rs.getInt(1));
                    RcloneConfig destination = (RcloneConfig) Const.palveluht.get(rs.getInt(10));
                    log.info("Source: " + source.type + " Destination: " + destination.type);
                    RcloneRun rr = new RcloneRun();
                    rr.config(source);
                    rr.config(destination);
                    String sourceToken = obscure(rs.getString(9), rr);
                    String destinationToken = obscure(rs.getString(19), rr);
                    log.info("SourceToken: " + sourceToken + " DestinationToken: " + destinationToken);
                    String access_key_id = rs.getString(6);
                    String secret_access_key = rs.getString(7);
                    source.access_key_id = (null != access_key_id) ? access_key_id : rs.getString(16);
                    source.secret_access_key =  (null != secret_access_key) ? secret_access_key : rs.getString(17);
                    source.polku = rs.getString(4);
                    rr.copy(source, destination, sourceToken, destinationToken);

                }
                Statement stmt = rs.getStatement();
                rs.close();
                stmt.close();
            }
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 10;
        }
        return 0;
    }

    private String obscure(String token, RcloneRun rr) {
        if (null == token) return null;
        else {
            return rr.obfuscate(token);
        }
    }

}
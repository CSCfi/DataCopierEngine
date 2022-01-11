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
                    String sourceToken = rs.getString(9);
                    String destinationToken = rs.getString(18);
                    log.info("SourceToken: " + sourceToken + " DestinationToken: " + destinationToken);
                    source.access_key_id = rs.getString(6);
                    source.secret_access_key = rs.getString(7);
                    destination.access_key_id = rs.getString(15);
                    destination.secret_access_key = rs.getString(16);
                    source.omistaja = rs.getString(3);
                    destination.omistaja = rs.getString(12);
                    source.polku = rs.getString(4);
                    destination.polku = rs.getString(13);
                    source.username = rs.getString(5);
                    destination.username  = rs.getString(14);
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

    /**
     * Poistettu käytöstöstä, koska ei toimi. Minulla on toimiviakin tokeneita,
     * joten lakkasiko jossain versiossa toimimasta. Oire toimimattomuudesta on, että
     * jokainen ajo tuottaa eri tuloksen: toimiva on tietysti pysyvä.
     *
     * @param token String selväkielinen Idan token
     * @param rr RcloneRun ajetaan obscure komento
     * @return String lievästi salattu token --webdav-pass optiolle
     */
    private String obscure(String token, RcloneRun rr) {
        if (null == token) return null;
        else {
            return rr.obfuscate(token);
        }
    }

}
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
                    log.info("Source: " + source.type + "Destination: " + destination.type);


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


}
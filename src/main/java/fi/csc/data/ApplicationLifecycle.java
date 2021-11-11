package fi.csc.data;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import io.agroal.api.AgroalDataSource;

import java.sql.Connection;
import java.sql.SQLException;


@QuarkusMain
public class ApplicationLifecycle implements QuarkusApplication {

    @Inject
    Logger log;

    @Inject
    AgroalDataSource defaultDataSource;

 @Override
    public int run(String... args) throws Exception {

     System.out.println("Hello " + args[1]);
     try  {
                Connection connection = defaultDataSource.getConnection();
                log.info(Base.read(connection));
                connection.close();
    } catch (SQLException throwables) {
         throwables.printStackTrace();
         return 10;
     }
    return 0;
    }


}
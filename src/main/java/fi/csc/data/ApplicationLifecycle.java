package fi.csc.data;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import io.agroal.api.AgroalDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


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
     try  {
         Connection connection = defaultDataSource.getConnection();
         ResultSet rs = Base.read(connection);
         if (null == rs) {
             log.info("Nothing to do, rs was null");
         } else {
             if (rs.first()) {
                 String source = (String) Const.palveluht.get(rs.getInt(1));
                 log.info("Source: "+source);

                 Process process = Runtime.getRuntime().exec("/work/rclone --version");
                 StreamGobbler streamGobbler =
                         new StreamGobbler(process.getInputStream(), System.out::println);
                 Executors.newSingleThreadExecutor().submit(streamGobbler);
                 int exitCode = process.waitFor();
                 assert exitCode == 0;
             }
         }
         connection.close();
    } catch (SQLException throwables) {
         throwables.printStackTrace();
         return 10;
     }
    return 0;
    }


private static class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines()
          .forEach(consumer);
    }
}
}
package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import fi.csc.data.model.Status;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.CommandLineArguments;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import io.agroal.api.AgroalDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.List;

@QuarkusMain
public class Main {
    public static void main(String... args) {
        Quarkus.run(Engine.class, args);
    }
}
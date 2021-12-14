package fi.csc.data;

import fi.csc.data.model.RcloneConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import static fi.csc.data.model.RcloneConfig.THES3END;


public class RcloneRun {

    static final String RCLONE = "/work/rclone "; //kontissa, muista synckronoida dockerfilen kanssa
    static final String CREATE = "config create ";
    static final String VÄLILYÖNTI = " ";

    public int config(RcloneConfig rc) {

        StringBuilder komento = new StringBuilder(RCLONE);
        komento.append(CREATE);
        komento.append(Const.cname.get(rc.palvelu));
        komento.append(VÄLILYÖNTI);
        komento.append(rc.type); //webdav or s3
        komento.append(VÄLILYÖNTI);
        if (rc.palvelu < 3) { //ida This is secure because all is the constants of this program
            komento.append("vendor=");
            komento.append(rc.vendor);
            komento.append(VÄLILYÖNTI);
            komento.append("url=");
            komento.append(rc.url);
        } else  if (!rc.open){ //allas
            if (!rc.env_auth)
               komento.append("env_auth=false");
            else
                komento.append("env_auth=true");
            komento.append(VÄLILYÖNTI);
            komento.append(THES3END);
        }

        try {
            Process process = Runtime.getRuntime().exec(komento.toString());

            RcloneRun.StreamGobbler streamGobbler =
                    new RcloneRun.StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
            return exitCode;
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -2;
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
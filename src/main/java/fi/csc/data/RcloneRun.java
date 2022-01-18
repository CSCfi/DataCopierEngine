package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import fi.csc.data.model.Status;
import io.netty.handler.codec.base64.Base64Encoder;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import static fi.csc.data.Const.ALLASPUBLIC;
import static fi.csc.data.Const.IDASTAGING;
import static fi.csc.data.model.RcloneConfig.ASETUKSET;
//import static fi.csc.data.model.RcloneConfig.THES3END;


public class RcloneRun {

    static final String RCLONE = "/work/rclone"; //kontissa, muista synckronoida dockerfilen kanssa
    //static final String CREATE = "config create ";
    static final String VÄLILYÖNTI = " ";
    static final String KAKSOISPISTE = ":";
    static final String PLUS = "+";
    static final String LAINAUSMERKKI ="\"";
    static final String AIKA = "Elapsed time:";
    static final String MB  = "Transferred:";
    static final String KAIKKI = "100%";
    static final double KILO = 1000;

    /**
     *   Yrittää parsia rclone -P tulostuksen: DSC08611.JPG:  2% /11.719Mi, 0/s, -Transferred:   	   11.719 MiB / 11.719 MiB, 100%, 0 B/s, ETA -
Transferred:            1 / 1, 100%
Elapsed time:         1.3s

     */
    Consumer<String> kaiva = s -> {
       if (s.contains(AIKA)) {
           String ss = s.substring(AIKA.length()+1,s.lastIndexOf("s")).trim();
           double kesto = Double.parseDouble(ss);
           System.out.println("Kesto: "+ kesto);
       } else if (s.contains(MB) && s.contains(KAIKKI)) {
           String ss = s.substring(MB.length()+1,s.lastIndexOf(KAIKKI));
           String[] identtiset = ss.split("/");
           String[] lukuyksikkö = identtiset[0].split(VÄLILYÖNTI);
           double luku = Double.parseDouble(lukuyksikkö[0].trim());
           if (lukuyksikkö[1].contains("KiB"))
               luku = luku/KILO;
           else if (lukuyksikkö[1].contains("GiB"))
               luku = luku*KILO;
           else if (lukuyksikkö[1].contains("TiB"))
               luku = luku*KILO*KILO;
            System.out.println("Megatavut: " + luku);
        }

    };

    /**
     * Run rclone config to create both source and destination. Write  .config/rclone/rclone.conf
     *
     * @param rc RcloneConfig source or destination
     * @return int status 0 is success
     */
    public Status config(RcloneConfig rc, String token) {

        ArrayList<String> komento = new ArrayList<String>(7);
        komento.add(RCLONE);
        komento.add("config");
        komento.add("create");
        komento.add((String)Const.cname.get(rc.palvelu));
        komento.add(String.valueOf(rc.type)); //webdav or s3
        if (rc.palvelu < 3) { //ida This is secure because all is the constants of this program
            komento.add("vendor=" + rc.vendor);
            komento.add("url=" + rc.url);
            komento.add("pass=" + token);
        } else  if (!rc.open){ //allas
            if (!rc.env_auth)
               komento.add("env_auth=false");
            else
                komento.add("env_auth=true");
            komento.add("access_key_id=" + token);
            komento.add(ASETUKSET[0]);
            komento.add(ASETUKSET[1]);
            komento.add(ASETUKSET[2]);
        }
        return realRun(komento.toArray(new String[komento.size()]));
    }

    /**
     * obscure token. rclone has nice security feature to not use clear tokens but somehow obfuscated
     * Just run a rclone command obscure
     *
     * @param token String Token, password to access nextcloud
     * @return String obfuscated token
     */
    public String obfuscate(String token) {
        try {
            Process process = Runtime.getRuntime().exec(RCLONE + "obscure "+token);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int exitCode = process.waitFor();
            assert exitCode == 0;
            return reader.readLine();
           } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "error in obscure";
    }

    /**
     * Sekä konfikuraatio että varsinainen rclone komennon suoritus
     *
     * @param komento String[] komento ja kaikki optiot, koko komeus suoritetetaan
     * @return Status, jossa int 0 jos kaikki meni hyvin, muuten virhekoodi
     */
    private Status realRun(String[] komento) {
        for (int i = 0; i < komento.length; i++) { // vain debuggaus: voi optimoida pois!
            if (null == komento[i]) {
                System.err.println(i + " was null after " + komento[i-1]);
                return new Status(-5);
            }
        }
        try {
            Process process = Runtime.getRuntime().exec(komento);

            RcloneRun.StreamGobbler streamGobbler =
                    new RcloneRun.StreamGobbler(process.getInputStream(), kaiva);
            RcloneRun.StreamGobbler errorStreamGobbler =
                    new RcloneRun.StreamGobbler(process.getErrorStream(), System.err::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;
            return  new Status(exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Status(-2);
    }

    /**
     * Kopio rclonella tiedoston tai hakemiston idan ja altaan välillä (ehkä muukin toimii)
     *
     * @param source RcloneConfig olio, jossa lähdejärjestelmän tiedot
     * @param destination RcloneConfig olio, jossa kohdejärjestelmän tiedot
     * @param sourceToken String Idan terminologiassa sovellussalasana, joita luodaan turvallisuus asetuksissa
     * @param destinationToken String kohdejärjestelmän sovellussalasana, vain toinen tarvitaan
     * @return int 0 jos kaikki meni hyvin, muuten virhekoodi
     */
    public Status copy(RcloneConfig source, RcloneConfig destination, String sourceToken, String destinationToken) {
        String[] komento = new String[9];
        komento[0] = RCLONE;
        if (source.open && (ALLASPUBLIC == source.palvelu)) {
            komento[1] = "copyurl";
            komento[2] = source.polku;
        } else {
             komento[1] = "copy";
             komento[2] = Const.cname.get(source.palvelu) +
             KAKSOISPISTE +
             source.omistaja;
             if (IDASTAGING == source.palvelu)
                 komento[2] = komento[2] + PLUS;
             komento[2] = komento[2] + source.polku;
        }

        komento[3] = Const.cname.get(destination.palvelu) +
        KAKSOISPISTE +
        destination.omistaja;
        if (IDASTAGING == destination.palvelu) {
            komento[3] = komento[3] + PLUS;
        }
        komento[3] = komento[3] + destination.polku;
        komento[4] = "--webdav-headers";
        // Ei toimine jos sekä source että destination webdav, eikä saa tulla ylimääräisiä salaisuuksia
       if ((null != sourceToken) && (null != source.username)) {
           komento[5] = "Authorization,Basic "+new String(Base64.getEncoder().
                   encode((source.username+":"+sourceToken).
                           getBytes(StandardCharsets.ISO_8859_1)));
       }
       if ((null != destinationToken) && (null != destination.username)) {
           komento[5] = "Authorization," + LAINAUSMERKKI +"Basic "+new String(Base64.getEncoder().
                   encode((destination.username+":"+destinationToken).
                           getBytes(StandardCharsets.ISO_8859_1)))+LAINAUSMERKKI;
       }
       komento[6] = "--s3-secret-access-key";
        // ei toimine, jos sekä source että destination s3
        if ((null != source.access_key_id) && (null != source.secret_access_key)) {
            komento[7] = source.secret_access_key;
        }
        if ((null != destination.access_key_id) && (null != destination.secret_access_key)) {
            komento[7] = destination.secret_access_key;
        }
        komento[8] = "-P";
        /*komento[8] = "-vv";
        komento[9] = "--dump";
        komento[10] =   "auth";*/
        //System.out.println(komento.toString());
        return realRun(komento);
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

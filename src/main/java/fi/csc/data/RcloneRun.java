package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import io.netty.handler.codec.base64.Base64Encoder;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import static fi.csc.data.Const.ALLASPUBLIC;
import static fi.csc.data.Const.IDASTAGING;
import static fi.csc.data.model.RcloneConfig.THES3END;


public class RcloneRun {

    static final String RCLONE = "/work/rclone"; //kontissa, muista synckronoida dockerfilen kanssa
    static final String CREATE = "config create ";
    static final String VÄLILYÖNTI = " ";
    static final String KAKSOISPISTE = ":";
    static final String PLUS = "+";
    static final String LAINAUSMERKKI ="\"";

    /**
     * Run rclone config to create both source and destination. Write  .config/rclone/rclone.conf
     *
     * @param rc RcloneConfig source or destination
     * @return int status 0 is success
     */
    public int config(RcloneConfig rc) {

        String[] komento = new String[7];
        komento[0] =RCLONE;
        komento[1] = "config";
        komento[2] = "create";
        komento[3] = (String)Const.cname.get(rc.palvelu);
        komento[4] = String.valueOf(rc.type); //webdav or s3
        if (rc.palvelu < 3) { //ida This is secure because all is the constants of this program
            komento[5] = "vendor=" + rc.vendor;
            komento[6] = "url=" + rc.url;
        } else  if (!rc.open){ //allas
            if (!rc.env_auth)
               komento[5] = "env_auth=false";
            else
                komento[5] = "env_auth=true";
            komento[6] = THES3END;
        }
        return realRun(komento);
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
     * @param komento String koko komentorivi, joka suoritetetaan
     * @return int 0 jos kaikki meni hyvin, muuten virhekoodi
     */
    private int realRun(String[] komento) {
        try {
            Process process = Runtime.getRuntime().exec(komento);

            RcloneRun.StreamGobbler streamGobbler =
                    new RcloneRun.StreamGobbler(process.getInputStream(), System.out::println);
            RcloneRun.StreamGobbler errorStreamGobbler =
                    new RcloneRun.StreamGobbler(process.getErrorStream(), System.err::println);
            Executors.newSingleThreadExecutor().submit(errorStreamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;
            return exitCode;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -2;
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
    public int copy(RcloneConfig source, RcloneConfig destination, String sourceToken, String destinationToken) {
        String[] komento = new String[6];
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
        // Ei toimine jos sekä source että destination webdav, eikä saa tulla ylimääräisiä salaisuuksia
       if ((null != sourceToken) && (null != source.username)) {
           komento[4] = komentoon(sourceToken, source.username);
       }
       if ((null != destinationToken) && (null != destination.username)) {
           komento[4] = komentoon(destinationToken,destination.username);
       }
        // ei toimine, jos sekä source että destination s3
        if ((null != source.access_key_id) && (null != source.secret_access_key)) {
            komento[5] = s3auth(source.access_key_id, source.secret_access_key);
        }
        if ((null != destination.access_key_id) && (null != destination.secret_access_key)) {
            komento[5] = s3auth(destination.access_key_id, destination.secret_access_key);
        }
        //System.out.println(komento.toString());
        return realRun(komento);
    }

    /**
     * Lisää --webdav-pass ja --webdav-user optiot komentoon
     *
     * @param token String Idan sovellussalasana
     * @param username String Idan käyttäjätunnus
     * @return String pätkä komentoriviä tai tyhjä merkkijono, jos jompikumpi parametri puuttui
     */
    private String komentoon(String token, String username) {

            StringBuilder webdav = new StringBuilder("--webdav-headers ");
            webdav.append(LAINAUSMERKKI);
            webdav.append("Authorization");
            webdav.append(LAINAUSMERKKI);
            webdav.append(",");
             webdav.append(LAINAUSMERKKI);
            webdav.append("Basic ");
            webdav.append(Base64.getEncoder().encodeToString((username+":"+token).getBytes(StandardCharsets.UTF_8)));
             webdav.append(LAINAUSMERKKI);
            webdav.append(VÄLILYÖNTI);
            return webdav.toString();

    }

    /**
     * Lisää --s3-access-key-id ja --s3-secret-access-key optiot komentoriviin
     *
     * @param access_key_id String Altaan salaisuuksia
     * @param secret_access_key String Altaan salaisuuksia
     * @return String pätkä komentoriviä tai tyhjä merkkijono, jos jompikumpi parametri puuttui
     */
    private String s3auth(String access_key_id, String secret_access_key) {
            StringBuilder s3 = new StringBuilder("--s3-access-key-id ");
            s3.append(access_key_id);
            s3.append(VÄLILYÖNTI);
            s3.append("--s3-secret-access-key ");
            s3.append(secret_access_key);
            return s3.toString();
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

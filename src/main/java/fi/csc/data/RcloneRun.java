package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import fi.csc.data.model.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

import static fi.csc.data.Const.ALLASPUBLIC;
import static fi.csc.data.Const.IDASTAGING;
import static fi.csc.data.model.RcloneConfig.ASETUKSET;

public class RcloneRun {

    static final String HOME = "/deployments/";
    static final String RCLONE = HOME+"rclone";
    static final String[] RCLONEHOME = {"HOME="+HOME};
    static final String CONFIG = "config";
    static final String KAKSOISPISTE = ":";
    static final String PLUS = "+";
    static final String LAINAUSMERKKI ="\"";
    static final String KAUTTA = "/";
    static final String VÄLILYÖNTI = " ";
    static final String AIKA = "Elapsed time:";

    int copyid;

    public RcloneRun(int id) {
        this.copyid = id;
    }

   public int delete(RcloneConfig rc) {
       ArrayList<String> komento = new ArrayList<>(4);
        komento.add(RCLONE);
        komento.add(CONFIG);
        komento.add("delete");
        komento.add(Const.cname.get(rc.palvelu)+copyid);
       try {
           Process process = Runtime.getRuntime().exec(komento.toArray(new String[komento.size()]), RCLONEHOME);
           return process.waitFor();
       } catch (IOException | InterruptedException e) {
           e.printStackTrace();
           return -1;
       }
   }

    /**
     * Run rclone config to create both source and destination. Write  .config/rclone/rclone.conf
     *
     * @param rc RcloneConfig source or destination
     * @return int status 0 is success
     */
    public int config(RcloneConfig rc, String token) {

        ArrayList<String> komento = new ArrayList<>(7);
        komento.add(RCLONE);
        komento.add("config");
        komento.add("create");
        komento.add(Const.cname.get(rc.palvelu)+copyid);
        komento.add(String.valueOf(rc.type)); //webdav or s3
        if (rc.palvelu < 3 || rc.palvelu > 6) { //ida || b2dropThis is secure because all is the constants of this program
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
         try {
           Process process = Runtime.getRuntime().exec(komento.toArray(new String[komento.size()]), RCLONEHOME);
           return process.waitFor();
       } catch (IOException | InterruptedException e) {
           e.printStackTrace();
           return -1;
       }
    }

    /**
     * Sekä konfikuraatio että varsinainen rclone komennon suoritus
     *
     * @param komento String[] komento ja kaikki optiot, koko komeus suoritetetaan
     * @return Status, jossa int 0 jos kaikki meni hyvin, muuten virhekoodi
     */
    private Status realRun(String[] komento, Seurantasäie ss) {
        for (int i = 0; i < komento.length; i++) { // vain debuggaus: voi optimoida pois!
            if (null == komento[i]) {
                System.err.println(i + " was null after " + komento[i-1]);
                return new Status(-5);
            }
        }
        long alkuaika = System.currentTimeMillis();
        try {
            Process process = Runtime.getRuntime().exec(komento, RCLONEHOME);

            StreamsHandling streamGobbler = new StreamsHandling(process.getInputStream(),
                    process.getErrorStream());
            if (null != ss) {
                ss.setStreamsHandling(streamGobbler);
                ss.updataStatus();
                System.out.println("Seurantasäie set");
            } else
                Executors.newSingleThreadExecutor().submit(streamGobbler);


            int exitCode = process.waitFor();
            int kesto = (int) ((System.currentTimeMillis()-alkuaika)/1000L);
            streamGobbler.update();
            assert exitCode == 0;
            Status s =  new Status(exitCode, streamGobbler.getMB(),
                                kesto,
                                streamGobbler.getNOFiles(),
                    streamGobbler.getErrors());
            if (null != ss) {
                ss.unregister(); //Seurannan poisto
                System.out.println("Seurantasäie poistettu");
            }
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException when running rclone:" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("InterruptedException when running rclone:" + e.getMessage());
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
    public Status copy(RcloneConfig source, RcloneConfig destination, String sourceToken,
                       String destinationToken, Seurantasäie ss) {
        ArrayList<String>  komento = new ArrayList<String>(10);
        StringBuilder apu = new StringBuilder(300); //more than 255. Note Initial, so no really matter
        komento.add(RCLONE);
        if (source.open && (ALLASPUBLIC == source.palvelu)) {
            komento.add("copyurl");
            komento.add(source.polku);
        } else {
             komento.add("copy");
             apu.append(Const.cname.get(source.palvelu));
             apu.append(copyid);
             apu.append(KAKSOISPISTE);
             apu.append(source.omistaja);
             if (IDASTAGING == source.palvelu)
                 apu.append(PLUS);
             apu.append(source.polku);
             komento.add(apu.toString());
        }
        apu.delete(0,apu.capacity());
        apu.append(Const.cname.get(destination.palvelu));
        apu.append(copyid);
        apu.append(KAKSOISPISTE);
        apu.append(destination.omistaja);
        if (IDASTAGING == destination.palvelu) {
            apu.append(PLUS);
        }
        apu.append(destination.polku);
        komento.add(apu.toString());
        komento.add("--webdav-headers");
        // Ei toimine jos sekä source että destination webdav, eikä saa tulla ylimääräisiä salaisuuksia
       if ((null != sourceToken) && (null != source.username)) {
           komento.add("Authorization,Basic "+new String(Base64.getEncoder().
                   encode((source.username+":"+sourceToken).
                           getBytes(StandardCharsets.ISO_8859_1))));
       }
       if ((null != destinationToken) && (null != destination.username)) {
           komento.add("Authorization," + LAINAUSMERKKI +"Basic "+new String(Base64.getEncoder().
                   encode((destination.username+":"+destinationToken).
                           getBytes(StandardCharsets.ISO_8859_1)))+LAINAUSMERKKI);
       }
       komento.add("--s3-secret-access-key");
        // ei toimine, jos sekä source että destination s3
        if ((null != source.access_key_id) && (null != source.secret_access_key)) {
            komento.add(source.secret_access_key);
        }
        if ((null != destination.access_key_id) && (null != destination.secret_access_key)) {
            komento.add(destination.secret_access_key);
        }
        komento.add("--s3-chunk-size");
        komento.add("100M");
        komento.add("-P");
        /*komento[8] = "-vv";
        komento[9] = "--dump";
        komento[10] =   "auth";*/
        //System.out.println(komento.toString());
        return realRun(komento.toArray(new String[komento.size()]), ss);
    }

}

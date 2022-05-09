package fi.csc.data;

import fi.csc.data.model.RcloneConfig;
import fi.csc.data.model.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static fi.csc.data.Const.ALLASPUBLIC;
import static fi.csc.data.Const.IDASTAGING;
import static fi.csc.data.model.RcloneConfig.ASETUKSET;


public class RcloneRun {

    static final String RCLONE = "/work/rclone";
    static final String CONFIG = "config";
    static final String KAKSOISPISTE = ":";
    static final String PLUS = "+";
    static final String LAINAUSMERKKI ="\"";
    static final String KAUTTA = "/";
    static final String VÄLILYÖNTI = " ";
    static final String AIKA = "Elapsed time:";
    static final String MB  = "Transferred:";
    static final String KAIKKI = "100%";
    static final double KILO = 1000;

    int copyid;

    public RcloneRun(int id) {
        this.copyid = id;
    }

   public int delete(RcloneConfig rc) {
       ArrayList<String> komento = new ArrayList<>(4);
        komento.add(RCLONE);
        komento.add(CONFIG);
        komento.add("delete");
        komento.add((String)Const.cname.get(rc.palvelu)+copyid);
       try {
           Process process = Runtime.getRuntime().exec(komento.toArray(new String[komento.size()]));
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
    public Status config(RcloneConfig rc, String token) {

        ArrayList<String> komento = new ArrayList<>(7);
        komento.add(RCLONE);
        komento.add("config");
        komento.add("create");
        komento.add((String)Const.cname.get(rc.palvelu)+copyid);
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
        long alkuaika = System.currentTimeMillis();
        try {
            Process process = Runtime.getRuntime().exec(komento);

            RcloneRun.StreamGobbler streamGobbler = new RcloneRun.StreamGobbler(process.getInputStream(),
                    process.getErrorStream());
            Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            int kesto = (int) ((System.currentTimeMillis()-alkuaika)/1000L);
            assert exitCode == 0;
            return  new Status(exitCode, streamGobbler.getMB(),
                                kesto,
                                streamGobbler.getNOFiles(),
                    streamGobbler.getErrors());
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
             komento[2] = Const.cname.get(source.palvelu) + copyid +
             KAKSOISPISTE +
             source.omistaja;
             if (IDASTAGING == source.palvelu)
                 komento[2] = komento[2] + PLUS;
             komento[2] = komento[2] + source.polku;
        }

        komento[3] = Const.cname.get(destination.palvelu) + copyid +
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

    private  class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private InputStream errorStream;
        List<String> list;
        StringBuilder sberrors = new StringBuilder();
        Double megatavut;
        int tiedostojenlukumäärä = -1;

        public StreamGobbler(InputStream inputStream, InputStream errorStream) {
            this.inputStream = inputStream;
            this.errorStream = errorStream;
        }

        @Override
        public void run() {
            list = new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .collect(Collectors.toList());
             new BufferedReader(new InputStreamReader(errorStream))
                     .lines().forEach(s -> sberrors.append(s));

        }

        String getErrors() {
            return sberrors.toString();
        }

        public int getMB() {
            if (null != megatavut)
                return (int) Math.round(megatavut);
            else {
                if (null != list) {
                    OptionalInt d = list.stream()
                            .filter(s -> s.contains(MB) && s.contains(KAIKKI))
                            .mapToInt(s -> laskeMB(s)).max();
                    if (d.isPresent())
                        return d.getAsInt();
                }
                return 0; //ehkä erillinen arvo en tiedälle
            }
        }


        /**
         * Yrittää parsia siirretyt MB.
         *
         * @param s String like Lasketaan MB: *                                  DSC08601.JPG:100% /14.344Mi, 14.331Mi/s, 0sTransferred:   	   14.344 MiB / 14.344 MiB, 100%, 11.831 MiB/s,
         * @return int MB
         */
        private int laskeMB(String s) {
            System.out.println("Lasketaan MB:"+ s);
            String ss = s.substring(MB.length() + 1, s.lastIndexOf(KAIKKI));
            String[] identtiset = ss.split(KAUTTA);
            if (identtiset.length > 2) {
                String[] lukuyksikkö = identtiset[2].split(VÄLILYÖNTI);
                if (lukuyksikkö.length > 8) {
                    int i = 6;
                    while (lukuyksikkö[i].isEmpty())
                        i++;
                    double luku = Double.parseDouble(lukuyksikkö[i].trim());
                    return toMB(luku, lukuyksikkö[i+1]); //Tämä on oikea tulos
                } else {
                    System.out.println("lukuyksikkö.lenght was " + lukuyksikkö.length);
                }
            } else {
                final int TOINEN = 1;
                String lkm = identtiset[TOINEN].trim().substring(0,identtiset[TOINEN].length()-3);
                System.out.println(lkm);
                if (lkm.contains("B")) {
                    String[] lukuyksikkö = lkm.split(VÄLILYÖNTI);
                    double luku = Double.parseDouble(lukuyksikkö[0].trim());
                    return toMB(luku, lukuyksikkö[1]);
                } else
                    tiedostojenlukumäärä = Integer.parseInt(lkm);
            }
            return 0;
        }

        /**
         *
         * @param luku double ihmiselle sopivassa yksikössä
         * @param lukuyksikkö KiB Mib GiB or TiB ei vielä PiB, koska ei testiä
         * @return int MB
         */
        private int  toMB(Double luku, String lukuyksikkö) {
            int mb;
            if (lukuyksikkö.contains("KiB"))
                mb = (int) Math.round(luku / KILO);
            else if (lukuyksikkö.contains("GiB"))
                mb = (int) Math.round(luku * KILO);
            else if (lukuyksikkö.contains("TiB"))
                mb = (int) Math.round(luku * KILO * KILO);
            else
                mb = (int) Math.round(luku);
            System.out.println("Megatavut: " + mb);
            return mb;
        }

        public int getNOFiles() {
            return tiedostojenlukumäärä;
        }
    }

}

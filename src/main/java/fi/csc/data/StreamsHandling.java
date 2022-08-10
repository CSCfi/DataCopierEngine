package fi.csc.data;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import org.jboss.logging.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static fi.csc.data.RcloneRun.KAUTTA;
import static fi.csc.data.RcloneRun.VÄLILYÖNTI;

public class StreamsHandling implements Runnable {

    static final double KILO = 1000;
    static final String MB  = "Transferred:";
    static final String PROSENTTI = "%";
    Logger log;
    private final BufferedInputStream binputStream;
    private final BufferedInputStream berrorStream;
    List<String> previousl;
    String input; //rclone output!
    StringBuilder sberrors = new StringBuilder();
    Double megatavut;
    int tiedostojenlukumäärä = -1;

    public StreamsHandling(InputStream inputStream, InputStream errorStream) {
        this.binputStream = new BufferedInputStream(inputStream);
        this.berrorStream = new BufferedInputStream(errorStream);
    }

    @Override
    public void run() {
        try {
            //binputStream.transferTo(System.out);
            log.debug(new String(berrorStream.readAllBytes(),StandardCharsets.UTF_8));
            log.info("Config success");
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public int update() {
        try {
            int available = binputStream.available();
            byte[] saatavilla = binputStream.readNBytes(available);
            input = new String(saatavilla, StandardCharsets.UTF_8);
            sberrors.append(new String(berrorStream.readNBytes(berrorStream.available()), StandardCharsets.UTF_8));
            return available;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
         return -1;
    }

    String getErrors() {
        return sberrors.toString();
    }

    public int getMB() {
            if (null != input) {
                OptionalInt d = input.lines()  //voisi ottaa myös "Elapsed time:"
                        .filter(s -> s.contains(MB))
                        .filter(s -> !s.contains("0 B"))
                        .filter(s -> !s.contains("0%"))
                        .mapToInt(s -> laskeMB(s)).max();
                if (d.isPresent())
                    return d.getAsInt();
                else if (null != megatavut)
                    return (int) Math.round(megatavut);
            }
            return 0; //ehkä erillinen arvo en tiedälle
    }


    /**
     * Yrittää parsia siirretyt MB.
     *
     * @param s String like Lasketaan MB: *                                  DSC08601.JPG:100% /14.344Mi, 14.331Mi/s, 0sTransferred:   	   14.344 MiB / 14.344 MiB, 100%, 11.831 MiB/s,
     * @return int MB
     */
    private int laskeMB(String s) {
        System.out.println("Lasketaan MB:"+ s);
        String ss = s.substring(MB.length() + 1, s.lastIndexOf(PROSENTTI));
        String[] identtiset = ss.split(KAUTTA);
        if (identtiset.length > 1) {
            String[] lukuyksikkö = identtiset[0].split(VÄLILYÖNTI);
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



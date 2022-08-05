package fi.csc.data;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import org.jboss.logging.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static fi.csc.data.RcloneRun.KAIKKI;
import static fi.csc.data.RcloneRun.KAUTTA;
import static fi.csc.data.RcloneRun.MB;
import static fi.csc.data.RcloneRun.VÄLILYÖNTI;

public class StreamsHandling {

    static final double KILO = 1000;
    Logger log;
    private final BufferedInputStream binputStream;
    private final BufferedInputStream berrorStream;
    List<String> previousl;
    String input;
    StringBuilder sberrors = new StringBuilder();
    Double megatavut;
    int tiedostojenlukumäärä = -1;

    public StreamsHandling(InputStream inputStream, InputStream errorStream) {
        this.binputStream = new BufferedInputStream(inputStream);
        this.berrorStream = new BufferedInputStream(errorStream);
    }
/*
    @Override
    public void run() {
        list = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.toList());
        new BufferedReader(new InputStreamReader(errorStream))
                .lines().forEach(s -> sberrors.append(s));

    }
*/
    public int update() {
        try {
            int available = binputStream.available();
            byte[] saatavilla = binputStream.readNBytes(available);
            input = new String(saatavilla, StandardCharsets.UTF_8);
            sberrors.append(berrorStream.readNBytes(berrorStream.available()));
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
                OptionalInt d = input.lines()
                        .filter(s -> s.contains(MB))
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



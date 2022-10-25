package fi.csc.data;

import org.jboss.logging.Logger;

import javax.inject.Inject;
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
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

import static fi.csc.data.RcloneRun.KAUTTA;


public class StreamsHandling implements Runnable {

    static final double KILO = 1000;
    static final String TRANSFERRRED = "Transferred:";
    static final String PROSENTTI = "%";
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
            System.out.println(new String(berrorStream.readAllBytes(),StandardCharsets.UTF_8));
            System.out.println("Config success");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public int update() {
        try {
            int available = binputStream.available();
            byte[] saatavilla = binputStream.readNBytes(available);
            input = new String(saatavilla, StandardCharsets.UTF_8);
            System.out.println("input was: "+input);
            sberrors.append(new String(berrorStream.readNBytes(berrorStream.available()), StandardCharsets.UTF_8));
            return available;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
         return -1;
    }

    String getErrors() {
        return sberrors.toString();
    }

    /**
     * Käy läpi rclonen tulostuksen ja selvitää suurimman tämän hetkisen MB määrän
     * @return int MB
     */
    public int getMB() {
            if (null != input) {
                OptionalInt d = input.lines()  //voisi ottaa myös "Elapsed time:"
                        .filter(s -> s.contains(TRANSFERRRED))
                        .filter(s -> !s.contains(" 0%"))
                        .mapToInt(this::laskeMB).max();
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
        String ss = s.substring(s.indexOf(TRANSFERRRED)+TRANSFERRRED.length() + 1, s.lastIndexOf(PROSENTTI));
        String[] identtiset = ss.split(KAUTTA);
        if (identtiset.length > 1) {
            Scanner sc = new Scanner(identtiset[0]);
            sc.findInLine("\\s+([0-9]*\\.?[0-9]*) ([kMGT])iB\\s");
            MatchResult result = sc.match();
            double luku = Double.parseDouble(result.group(1));
            return toMB(luku, result.group(2)); //Tämä on oikea tulos
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

    /**
     * Lukee rclonen tulostuksen ja valitsee "Transferred:            1 / 1, 100% rivin" kaltaisen rivin,
     * jonka perusteella laskee siirrettyjen tiedojen määrän
     *
     * @return int siirrettyjen tiedojen lukumäärä
     */
    public int getNOFiles() {
          if (null != input) {
                OptionalInt d = input.lines()
                        .filter(s -> s.contains(TRANSFERRRED))
                        .filter(s -> !s.contains("ETA"))
                        .filter(s -> !s.contains(" 0%"))
                        .mapToInt(this::laskeKPL).max();
                if (d.isPresent())
                    return d.getAsInt();
                else {
                    System.out.println("Tiedostojen lukumäärästä ei tolkkua!");
                    return tiedostojenlukumäärä;
                }
            }
            System.out.println("Tiedostojenlukumäärän laskemisen input oli tyhjä.");
            return 0; //ehkä erillinen arvo en tiedälle
    }

    /**
     * Parsii "Transferred:            1 / 1, 100% rivin" ja yrittää kaivaa ensimmäisen numeron
     * @param s String ylläolevan kaltainen rivi rclonen tulostuksesta
     * @return int Siirrettyjen tiedostojen lukumäärä (tai -1 jos tuli ongelmia)
     */
    private int laskeKPL(String s) {
        try {
            Scanner sc = new Scanner(s);
            sc.findInLine(TRANSFERRRED + "\\s+([0-9]+) / [0-9]+, [0-9]+%");
            MatchResult result = sc.match();
            return Integer.parseInt(result.group(1));
        } catch (IllegalStateException ise) {
            System.err.println(ise.getMessage()+s);
            return -1;
        }
    }


}



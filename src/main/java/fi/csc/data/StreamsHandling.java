package fi.csc.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static fi.csc.data.RcloneRun.KAIKKI;
import static fi.csc.data.RcloneRun.KAUTTA;
import static fi.csc.data.RcloneRun.MB;
import static fi.csc.data.RcloneRun.VÄLILYÖNTI;

public class StreamsHandling implements Runnable {

    static final double KILO = 1000;
    private final InputStream inputStream;
    private final InputStream errorStream;
    List<String> list;
    StringBuilder sberrors = new StringBuilder();
    Double megatavut;
    int tiedostojenlukumäärä = -1;

    public StreamsHandling(InputStream inputStream, InputStream errorStream) {
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



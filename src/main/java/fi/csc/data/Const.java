package fi.csc.data;

import java.util.Hashtable;

public class Const {

    static final String IDA = "https://ida.fairdata.fi/remote.php/webdav";
    static final String ALLAS = "a3s.fi";

    public static final Hashtable palveluht = new Hashtable<Integer, String>();

    static {
        palveluht.put(1, IDA);
        palveluht.put(2, IDA);
        palveluht.put(5, ALLAS);
        palveluht.put(6, ALLAS);
    }
}

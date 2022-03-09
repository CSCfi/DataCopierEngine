package fi.csc.data;

import fi.csc.data.model.RcloneConfig;

import java.util.Hashtable;

import static fi.csc.data.model.RcloneConfig.Type.s3;
import static fi.csc.data.model.RcloneConfig.Type.webdav;
import static fi.csc.data.model.RcloneConfig.Vendor.nextcloud;

public class Const {

    public static final int IDA = 1;
    public static final int IDASTAGING = 2;
    public static final int FAIRDATAOPEN = 3;
    public static final int FAIRDATACLOSED = 4;
    public static final int ALLAS = 5;
    public static final int ALLASPUBLIC = 6;
    public static final int B2DROP = 7;

    static final String WEBDAV = "remote.php/webdav";
    static final String IDAURL = "https://ida.fairdata.fi/"+WEBDAV;
    static final String IDAS = "ida";
    static final String ALLASS = "allas";
    //static final String ALLASURL = "a3s.fi";

    public static final Hashtable<Integer, RcloneConfig> palveluht = new Hashtable<>();
    public static final Hashtable<Integer, String> cname = new Hashtable<>();

    static {
        palveluht.put(IDA, new RcloneConfig(1, webdav, nextcloud, IDAURL, false));
        palveluht.put(IDASTAGING, new RcloneConfig(2, webdav, nextcloud, IDAURL, true));
        palveluht.put(ALLAS, new RcloneConfig(5, s3, false, false));
        palveluht.put(ALLASPUBLIC, new RcloneConfig(6, s3, false, true));
        palveluht.put(B2DROP, new RcloneConfig(7,  webdav, nextcloud,
                "https://b2drop.eudat.eu/"+WEBDAV, false));

        cname.put(IDA, IDAS);
        cname.put(IDASTAGING, IDAS);
        cname.put(ALLAS, ALLASS);
        cname.put(ALLASPUBLIC, ALLASS);
        cname.put(B2DROP, "b2drop");
    }
}

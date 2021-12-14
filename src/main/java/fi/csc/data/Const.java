package fi.csc.data;

import fi.csc.data.model.RcloneConfig;

import java.util.Hashtable;

import static fi.csc.data.model.RcloneConfig.Type.s3;
import static fi.csc.data.model.RcloneConfig.Type.webdav;
import static fi.csc.data.model.RcloneConfig.Vendor.nextcloud;

public class Const {

    static final String IDAURL = "https://ida.fairdata.fi/remote.php/webdav";
    static final String IDA = "ida";
    static final String ALLAS = "allas";
    static final String ALLASURL = "a3s.fi";

    public static final Hashtable palveluht = new Hashtable<Integer, RcloneConfig>();
    public static final Hashtable cname = new Hashtable<Integer, String>();

    static {
        palveluht.put(1, new RcloneConfig(1, webdav, nextcloud, IDAURL, false));
        palveluht.put(2, new RcloneConfig(2, webdav, nextcloud, IDAURL, true));
        palveluht.put(5, new RcloneConfig(5, s3, false, false));
        palveluht.put(6, new RcloneConfig(6, s3, false, true));

        cname.put(1, IDA);
        cname.put(2, IDA);
        cname.put(5, ALLAS);
        cname.put(6, ALLAS);
    }
}

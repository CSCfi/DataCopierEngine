package fi.csc.data;

import fi.csc.data.model.RcloneConfig;

import java.util.Hashtable;

import static fi.csc.data.model.RcloneConfig.Type.s3;
import static fi.csc.data.model.RcloneConfig.Type.webdav;
import static fi.csc.data.model.RcloneConfig.Vendor.nextcloud;

public class Const {

    static final String IDAURL = "https://ida.fairdata.fi/remote.php/webdav";
    static final String ALLASURL = "a3s.fi";

    public static final Hashtable palveluht = new Hashtable<Integer, RcloneConfig>();

    static {
        palveluht.put(1, new RcloneConfig(webdav, nextcloud, IDAURL, false));
        palveluht.put(2, new RcloneConfig(webdav, nextcloud, IDAURL, true));
        palveluht.put(5, new RcloneConfig(s3, false, false));
        palveluht.put(6, new RcloneConfig(s3, false, true));
    }
}

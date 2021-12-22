package fi.csc.data.model;

public class RcloneConfig {

    public final static String THES3END = "provider=Other endpoint=a3s.fi acl=private";
    public enum Type {swift, s3, webdav}
    public enum Vendor { nextcloud }

    public final int palvelu;
    public final Type type;
    public /*final*/ Vendor vendor;
    public /*final*/ String url;
    public /*final*/ boolean env_auth;
    public /*final*/ boolean staging;
    public /*final*/ boolean open;
    public String token;
    public String access_key_id;
    public String secret_access_key;
    public String omistaja; //project tai Bucket
    public String polku;
    public String username;


    public RcloneConfig(int palvelu, Type type, Vendor vendor, String url, boolean staging) {
        this.palvelu = palvelu;
        this.type = type;
        this.vendor = vendor;
        this.url = url;
        this.staging = staging;
    }

    public RcloneConfig(int palvelu, Type type, boolean env_auth, boolean open) {
        this.palvelu = palvelu;
         this.type = type;
         this.env_auth = env_auth;
         this.open  = open;
    }
}

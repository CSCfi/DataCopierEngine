package fi.csc.data.model;

public class RcloneConfig {

    public final static String THES3END = "provider=Other endpoint=a3s.fi acl=private";
    public enum Type {swift, s3, webdav}
    public enum Vendor { nextcloud }

    public int palvelu;
    public Type type;
    public Vendor vendor;
    public String url;
    public boolean env_auth;
    public boolean staging;
    public boolean open;

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

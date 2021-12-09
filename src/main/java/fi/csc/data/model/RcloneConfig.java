package fi.csc.data.model;

public class RcloneConfig {

    final static String[] THES3END = {"provider = Other", "endpoint = a3s.fi", "acl = private"};
    public enum Type {swift, s3, webdav}
    public enum Vendor { nextcloud }

    public Type type;
    public Vendor vendor;
    public String url;
    public boolean env_auth;
    public boolean staging;
    public boolean open;

    public RcloneConfig(Type type, Vendor vendor, String url, boolean staging) {
        this.type = type;
        this.vendor = vendor;
        this.url = url;
        this.staging = staging;
    }

    public RcloneConfig(Type type, boolean env_auth, boolean open) {
         this.type = type;
         this.env_auth = env_auth;
         this.open  = open;
    }
}

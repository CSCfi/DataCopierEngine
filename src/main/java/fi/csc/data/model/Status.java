package fi.csc.data.model;

public class Status {
    public int exitCode;
    public int  MB;
    public double kesto;
    public int files;
    public String errors;

    public Status(int exitCode) {
        this.exitCode = exitCode;
    }

    public Status(int exitCode, int mb, double kesto, int files, String error) {
        this.exitCode = exitCode;
        this.MB = mb;
        this.kesto = kesto;
        this.files = files;
        this.errors = error;
    }
}

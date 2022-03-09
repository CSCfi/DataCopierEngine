package fi.csc.data.model;

public class Status {
    public int exitCode;
    public int  MB;
    public double kesto;
    public int files;

    public Status(int exitCode) {
        this.exitCode = exitCode;
    }

    public Status(int exitCode, int mb, double kesto, int files) {
        this.exitCode = exitCode;
        this.MB = mb;
        this.kesto = kesto;
        this.files = files;
    }
}

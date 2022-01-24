package fi.csc.data.model;

public class Status {
    public int exitCode;
    public int  MB;
    public double kesto;

    public Status(int exitCode) {
        this.exitCode = exitCode;
    }

    public Status(int exitCode, int mb, double kesto) {
        this.exitCode = exitCode;
        this.MB = mb;
        this.kesto = kesto;
    }
}

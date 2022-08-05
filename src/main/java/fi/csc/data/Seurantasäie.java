package fi.csc.data;

public class Seurantasäie {
    Base db;
    SeurantaBean sb;
    StreamsHandling sh;

    public Seurantasäie(Base db, SeurantaBean sb) {
        this.db = db;
        this.sb = sb;
    }

    public void setStreamsHandling(StreamsHandling streamGobbler) {
        this.sh = streamGobbler;
        sb.register(this);
    }

    public void updataStatus(){
        int avail = sh.update();
        if (avail > 0) {
            int mb = sh.getMB();
            int files = sh.getNOFiles();
            db.update(mb, files);
        }
    }

    public void unregister() {
        sb.remove(this);
    }
}

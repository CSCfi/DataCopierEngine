package fi.csc.data;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class Seurantasäie {
    Base db;
    StreamsHandling sh;
    private AtomicInteger counter = new AtomicInteger();
    public Seurantasäie(Base db) {
        this.db = db;
    }

    public void setStreamsHandling(StreamsHandling streamGobbler) {
        this.sh = streamGobbler;
    }


     public int get() {
        return counter.get();
    }

    @Scheduled(every="10s")
    void increment() {
        counter.incrementAndGet();
        updataStatus();
    }

    private void updataStatus(){
        int mb = sh.getMB();
        int files = sh.getNOFiles();
        db.update(mb, files);
    }
}

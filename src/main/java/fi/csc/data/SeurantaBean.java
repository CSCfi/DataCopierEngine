package fi.csc.data;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
@ApplicationScoped
public class SeurantaBean {

    static final String AJASTUS = "10s";
    Logger log;
    List<Seurantasäie> ssl = new ArrayList<>();

    public void register(Seurantasäie ss)  {
        ssl.add(ss);
    }

    public void remove(Seurantasäie ss) {
        ssl.remove(ss);
    }

    @Scheduled(every=AJASTUS)
    void increment() {
        log.info(AJASTUS);
        if (null != ssl && !ssl.isEmpty())
            ssl.forEach(ss -> ss.updataStatus());
    }
}

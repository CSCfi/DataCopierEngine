package fi.csc.data;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SeurantaBean {
    List<Seurantasäie> ssl;

    public void register(Seurantasäie ss)  {
        ssl.add(ss);
    }

    public void remove(Seurantasäie ss) {
        ssl.remove(ss);
    }

    @Scheduled(every="10s")
    void increment() {
       if (null != ssl && !ssl.isEmpty())
        ssl.forEach(ss -> ss.updataStatus());
    }
}

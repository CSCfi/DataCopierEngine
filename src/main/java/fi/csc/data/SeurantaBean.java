package fi.csc.data;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
@ApplicationScoped
public class SeurantaBean {

    static final String AJASTUS = "10s";

    List<Seurantasäie> ssl = new ArrayList<>();

    public void register(Seurantasäie ss)  {
        System.out.println("Seurantasäiettä asetetaan");
        ssl.add(ss);
        if (ssl.isEmpty()) System.out.println("Seurantasäieasetus epäonnistui: "+ ssl.size());
    }

    public void remove(Seurantasäie ss) {
        ssl.remove(ss);
    }

    @Scheduled(every=AJASTUS)
    void increment() {
        if (ssl.isEmpty())
            System.out.println("ssl was empty");
        else {
            System.out.println(AJASTUS);
            ssl.forEach(Seurantasäie::updataStatus);
        }
    }
}

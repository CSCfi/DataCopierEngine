package fi.csc.data;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
@ApplicationScoped
public class SeurantaBean {

    static final String AJASTUS = "10s";

    Seurantasäie ss;
    ArrayList<Seurantasäie> ssl = new ArrayList<>();

    public void register(Seurantasäie ss)  {
        System.out.println("Seurantasäiettä asetetaan");
        this.ss = ss;
        //if (ssl.isEmpty()) System.out.println("Seurantasäieasetus epäonnistui: "+ ssl.size());
    }

    public void remove(Seurantasäie ss) {
        System.out.println("Seurantasäie poistetiin");
        ssl.remove(ss);
    }

    @Scheduled(every=AJASTUS)
    void increment() {
        if ( null == ss)
            System.out.println("ss was null");
        else {
            System.out.println(AJASTUS);
            ss.updataStatus();
            //ssl.forEach(Seurantasäie::updataStatus);
        }
    }

    public ArrayList<Seurantasäie> getSsl() {
        return ssl;
    }

}

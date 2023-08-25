package fi.csc.data;

public class Seurantasäie  extends Thread {
    Base db;
    StreamsHandling sh;

    boolean running;

    public Seurantasäie(Base db) {
        this.db = db;
    }

    public void run() {
        running = true;
         while (running) {
             System.out.println(this.getName() + " säie aktiivinen " + System.currentTimeMillis());
             try {
                 Thread.sleep(3000);
                 updataStatus();
             } catch (InterruptedException e) {
                System.out.println("InterruptedException: "+e.getMessage());
             }

         }
    }

    public void setRunning(boolean b) {
        this.running = b;
    }
    public void setStreamsHandling(StreamsHandling streamGobbler) {
        this.sh = streamGobbler;
    }

    public void updataStatus(){
        System.out.println("Updating...");
        int avail = sh.update();
        if (avail > 0) {
            int mb = sh.getMB();
            int files = sh.getNOFiles();
            db.update(mb, files);
        }
    }

}

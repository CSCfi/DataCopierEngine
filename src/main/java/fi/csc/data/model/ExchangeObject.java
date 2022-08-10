package fi.csc.data.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExchangeObject implements Runnable {
    BlockingQueue<String> tunnus = new LinkedBlockingQueue<>();
    BlockingQueue<String> sähköposti = new LinkedBlockingQueue<>();
    String emailaddress;

    public void lähetäTunnus(String tunnus) {
        try {
            this.tunnus.put(tunnus);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTunnus() {
        try {
            return this.tunnus.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSähköposti(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    @Override
    public void run() {
        try {
            this.sähköposti.put(emailaddress);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getEmailaddress() {
        try {
            return this.sähköposti.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

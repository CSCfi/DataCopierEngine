package fi.csc.data;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import fi.csc.data.model.CopyRequest;
import static fi.csc.data.CoreResource.QUEQUENAME;

@ApplicationScoped
public class CopyEngine implements Runnable {

    @Inject
    ConnectionFactory connectionFactory;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue(QUEQUENAME));
            while (true) {
                Message message = consumer.receive();
                if (message == null) return;
                CopyRequest cr = message.getBody(CopyRequest.class);
                System.out.println("Vastaanotettu: ");
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
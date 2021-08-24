package fi.csc.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import org.jboss.logging.Logger;
import io.quarkus.runtime.StartupEvent;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
/*import software.amazon.awssdk.auth.AWSCredentials;
import software.amazon.awssdk.auth.AWSStaticCredentialsProvider;
import software.amazon.awssdk.auth.BasicAWSCredentials;*/

@ApplicationScoped
public class ApplicationLifecycle {

    @Inject
    Logger log;
    /*private static final String FILENAME = "~/.s3cfg";
    private static final String SECTRETKEY = "secret_key =";
    private static final String ACCESSKEY = "access_key =";*/
    //software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient

    void onStart(@Observes StartupEvent event) {
        log.info("Running");
        /*String[] keys = lueTiedosto(FILENAME);
        String accesskey = keys[0];
        String secretkey = keys[1];
        AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);*/
        S3Client s3 = S3Client.builder()
                .region(Region.of("US"))
                //.withCredentials(new AWSStaticCredentialsProvider(credentials))
                .endpointOverride(URI.create("https://a3s.fi")).build();
        //ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3.listBuckets();
        listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name()));
    }

    /*
    private static String[] lueTiedosto(String filename) {

        String[] keys = new String[2];
        String inputLine;
        Boolean eivielälöydetty = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.startsWith(SECTRETKEY))
                    keys[1] =  inputLine.substring(SECTRETKEY.length()).trim();
                if (eivielälöydetty && inputLine.startsWith(ACCESSKEY)) {
                    keys[0] = inputLine.substring(ACCESSKEY.length()).trim();
                    eivielälöydetty = false;
                }
            }
        }  catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }*/
}
package fi.csc.data;

import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;
import io.quarkus.runtime.StartupEvent;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

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
        //log.info("Running");

        /*AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);*/
        S3Client s3 = S3Client.builder()
                .region(Region.of("US"))
                //.withCredentials(new AWSStaticCredentialsProvider(credentials))
                .endpointOverride(URI.create("https://a3s.fi")).build();
        ListBucketsResponse listBucketsResponse = s3.listBuckets();
        Bucket b = listBucketsResponse.buckets().stream().
                filter(x -> x.name().equals("public")).findFirst().orElse(null);
        if (null != b) {
            //log.info("Buketti löytyi");
            ListObjectsV2Iterable paginator = s3.listObjectsV2Paginator(
                    ListObjectsV2Request.builder()
                            .bucket(b.name())
                            .encodingType("UTF-8").build());
            //List<S3Object> objects = result.contents();
            Iterator<CommonPrefix> foldersIterator = paginator.commonPrefixes().iterator();
            /*while (foldersIterator.hasNext()) {
                foldersIterator.next();
            }*/
            AtomicInteger n = new AtomicInteger();
            for (ListObjectsV2Response page : paginator) {
                page.contents().forEach((S3Object object) -> {
                    n.getAndIncrement();
                    System.out.println(object.key());
                });
            }
            log.info("Sisältää: "+n);
       }
    }


}
package fi.csc.data;

import java.net.URI;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

public class Main {

    //software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient

    public static void main(String[] args) {
        S3Client s3 = S3Client.builder().endpointOverride(URI.create("https://a3s.fi")).build();

    }
}

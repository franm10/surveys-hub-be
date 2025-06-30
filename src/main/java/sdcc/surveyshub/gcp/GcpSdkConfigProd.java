package sdcc.surveyshub.gcp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import sdcc.surveyshub.exception.BucketInjectException;
import sdcc.surveyshub.utils.BucketUtils;

import java.io.IOException;
@Configuration
@Profile("prod")
@Slf4j
public class GcpSdkConfigProd {

    @Value("${google.bucket.name}")
    private String bucketName;

    @PostConstruct
    public void initFirebaseApp() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId(ServiceOptions.getDefaultProjectId())
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[Firebase][prod] FirebaseApp initialized with ADC and project ID: {}", ServiceOptions.getDefaultProjectId());
            }
        } catch (IOException e) {
            log.error("[Firebase][prod] Error initializing FirebaseApp", e);
        }

        try {
            BucketUtils.initialize(bucketName);
            log.info("[Firebase][prod] BucketUtils initialized with bucket: {}", bucketName);
        } catch (BucketInjectException e) {
            log.error("[Firebase][prod] Error injecting bucket name in BucketUtils", e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance(FirebaseApp.getInstance());
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean
    public Bucket bucket(Storage storage) {
        return storage.get(bucketName);
    }
}
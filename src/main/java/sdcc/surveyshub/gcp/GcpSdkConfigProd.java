package sdcc.surveyshub.gcp;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
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

    /**
     * Nel profilo prod ci affidiamo alle ADC di GCP:
     * - Cloud Run / Compute Engine fornisce automaticamente GoogleCredentials
     * - Il service-account associato al runtime deve avere i ruoli necessari
     */
    @PostConstruct
    public void initFirebaseApp() {
        // Se non è già inizializzato (es. local emulator), inizializzo con default credentials
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp();
            log.info("[Firebase][prod] Initialized FirebaseApp with Application Default Credentials");
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

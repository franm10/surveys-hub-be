package sdcc.surveyshub.gcp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import sdcc.surveyshub.exception.BucketInjectException;
import sdcc.surveyshub.utils.BucketUtils;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Profile("dev")
@Slf4j
public class GcpSdkConfigDev {

    @Value("${google.firebase.project-id}")
    private String projectId;

    @Value("${google.firebase.credentials-file}")
    private String credentialsFile;

    @Value("${google.bucket.name}")
    private String bucketName;

    @PostConstruct
    public void initFirebaseApp() {
        try {
            FileInputStream serviceAccount = new FileInputStream(credentialsFile);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            if( FirebaseApp.getApps().isEmpty() )
                FirebaseApp.initializeApp(options);

            log.info("[Firebase] FirebaseApp initialized with project: {}", projectId);
        }catch( IOException e ) {
            log.error("[Firebase] Error initializing FirebaseApp", e);
        }

        try {
            BucketUtils.initialize(bucketName);
            log.info("[Firebase] BucketUtils initialized with bucket: {}", bucketName);
        }catch( BucketInjectException e ) {
            log.error("[Firebase] Error inject bucket name in BucketUtils.class", e);
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
    public Storage storage() throws IOException {
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsFile)))
                .build()
                .getService();
    }

    @Bean
    public Bucket bucket(Storage storage) {
        return storage.get(bucketName);
    }

}

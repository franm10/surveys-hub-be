package sdcc.surveyshub.model.dao;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sdcc.surveyshub.exception.FirestoreIOException;
import sdcc.surveyshub.model.entity.Stats;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsDao {

    private static final String ERROR_MESSAGE = "Firestore IO error";

    private final Firestore firestore;

    public Stats findBySurveyId(String surveyId) {
        return execute(
                () -> {
                    DocumentSnapshot snap = firestore
                            .collection("surveys")
                            .document(surveyId)
                            .collection("stats")
                            .document("stats")
                            .get()
                            .get();
                    return snap.exists() ? snap.toObject(Stats.class) : null;
                },
                String.format("finding stats for survey %s", surveyId)
        );
    }

    public Stats create(Stats stats) {
        execute( () -> firestore.collection("surveys")
                                .document(stats.getId())
                                .collection("stats")
                                .document("stats")
                                .set(stats)
                                .get(),
                String.format("create stats document for survey %s", stats.getId())
        );
        return stats;
    }

    @SuppressWarnings("DuplicatedCode")
    private <T> T execute(Callable<T> action, String operation) {
        try {
            return action.call();
        }catch( InterruptedException ie ) {
            Thread.currentThread().interrupt();
            log.error("[Firestore][QuestionDao] Interrupted {}", operation, ie);
            throw new FirestoreIOException(ERROR_MESSAGE);
        }catch( ExecutionException ee ) {
            Throwable cause = ee.getCause();
            log.error("[Firestore][QuestionDao] Error {}: {}", operation, cause==null ? ee.getMessage() : cause.getMessage(), ee);
            throw new FirestoreIOException(ERROR_MESSAGE);
        }catch( Exception ex ) {
            log.error("[Firestore][QuestionDao] Unexpected error {}: {}", operation, ex.getMessage(), ex);
            throw new FirestoreIOException(ERROR_MESSAGE);
        }
    }

}
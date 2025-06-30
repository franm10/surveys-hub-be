package sdcc.surveyshub.model.dao;

import com.google.cloud.firestore.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.exception.FirestoreIOException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionDao {

    private static final String ERROR_MESSAGE = "Firestore IO error";

    private final Firestore firestore;

    public List<Question> findAllBySurveyId(String surveyId) {
        return execute(
                () ->   firestore.collection("surveys")
                        .document(surveyId)
                        .collection("questions")
                        .get()
                        .get()
                        .getDocuments()
                        .stream()
                        .map(doc -> doc.toObject(Question.class))
                        .toList(),
                String.format("finding questions for survey %s", surveyId)
        );
    }

    /* Location Saved: "surveys/{surveyId}/questions/{id}" */
    public Question save(Question q) {
        execute(
            () ->   firestore.collection("surveys")
                            .document(q.getSurveyId())
                            .collection("questions")
                            .document(q.getId())
                            .set(q)
                            .get(),
                    String.format("saving question %s from survey %s", q.getSurveyId(), q.getId())
        );
        return q;
    }

    // Versione migliorabile con Write Batch
    public void saveAll(List<Question> questions) {
        for( Question q : questions )
            save(q);
    }

    public void deleteAllFromSurvey(String surveyId) {
        execute(
            () ->   {
                        var col = firestore.collection("surveys")
                                            .document(surveyId)
                                            .collection("questions");
                        var docs = col.get().get().getDocuments();
                        if (!docs.isEmpty()) {
                            WriteBatch batch = firestore.batch();
                            docs.forEach(doc -> batch.delete(doc.getReference()));
                            batch.commit().get();
                        }
                        return null;
                    },
                    String.format("deleting questions for survey %s", surveyId)
        );
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

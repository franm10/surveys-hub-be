package sdcc.surveyshub.model.dao;

import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sdcc.surveyshub.exception.FirestoreIOException;
import sdcc.surveyshub.model.entity.SubmissionSurvey;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionSurveyDao {

    private static final String ERROR_MESSAGE = "Firestore IO error";

    private final Firestore firestore;

    public Optional<SubmissionSurvey> findBySurveyIdAndUserId(String surveyId, String userId) {
        return execute(
                () -> {
                        DocumentSnapshot doc = firestore.collection("surveys")
                                                                        .document(surveyId)
                                                                        .collection("submissions")
                                                                        .document(userId)
                                                                        .get()
                                                                        .get();
                        if( !doc.exists() )
                            return Optional.empty();

                        return Optional.ofNullable(doc.toObject(SubmissionSurvey.class));
                      },
                String.format("reading submission survey %s for user %s", surveyId, userId));
    }

    public Optional<SubmissionSurvey> findBySurveyIdAndUserEmail(String surveyId, String email) {
        return execute(
                () -> {
                        QuerySnapshot snap = firestore.collection("surveys")
                                                        .document(surveyId)
                                                        .collection("submissions")
                                                        .whereEqualTo("submittedByEmail", email)
                                                        .limit(1)
                                                        .get()
                                                        .get();
                        if( snap.isEmpty() )
                            return Optional.empty();
                        return Optional.of(snap.getDocuments().get(0).toObject(SubmissionSurvey.class));
                      },
                String.format("reading submission survey %s for email %s", surveyId, email)
        );
    }

    public List<String> findByUserId(String userId) {
        return execute(
                () -> firestore.collectionGroup("submissions")
                        .whereEqualTo("id", userId)          // 🔹 filtro sul campo, non sul doc-id
                        .get()
                        .get()
                        .getDocuments()
                        .stream()
                        .map(doc -> Objects.requireNonNull(doc.getReference()
                                                                .getParent()   // .../submissions
                                                                .getParent())   // .../surveys/{surveyId}
                                                                .getId())
                        .distinct()
                        .toList(),
                String.format("listing surveys answered by user %s", userId)
        );
    }

    public List<String> findParticipatedUserEmailsBySurveyId(String surveyId) {
        return execute(
                () -> {
                    QuerySnapshot snap = firestore.collection("surveys")
                                                    .document(surveyId)
                                                    .collection("submissions")
                                                    .get()
                                                    .get();
                    return snap.getDocuments().stream()
                            .map(doc -> doc.getString("submittedByEmail"))
                            .filter(Objects::nonNull)
                            .toList();
                },
                String.format("listing respondent emails for survey %s", surveyId)
        );
    }

    public SubmissionSurvey save(SubmissionSurvey ss) {
        execute(
                () ->   firestore.collection("surveys")
                        .document(ss.getSurveyId())
                        .collection("submissions")
                        .document(ss.getId())
                        .set(ss)
                        .get(),
                String.format("saving submission from survey %s and user %s", ss.getSurveyId(), ss.getId())
        );
        return ss;
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

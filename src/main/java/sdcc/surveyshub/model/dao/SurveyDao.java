package sdcc.surveyshub.model.dao;

import com.google.cloud.firestore.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import sdcc.surveyshub.model.entity.Survey;
import sdcc.surveyshub.exception.FirestoreIOException;
import sdcc.surveyshub.utils.DateUtils;
import sdcc.surveyshub.utils.enums.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyDao {

    private static final String ERROR_MESSAGE = "Firestore IO error";

    private final Firestore firestore;

    public Optional<Survey> findById(String id) {
        return execute(
            () ->   {
                        DocumentSnapshot snap = firestore.collection("surveys")
                                                        .document(id)
                                                        .get()
                                                        .get();
                        Survey survey = snap.toObject(Survey.class);
                        checkAndCloseIfExpired(survey);
                        return Optional.ofNullable(survey);
                    },
                    String.format("finding survey %s", id)
        );
    }

    public Optional<Survey> findByIdAndOwnerId(String id, String ownerId) throws AccessDeniedException {
        Optional<Survey> opt = findById(id);
        if( opt.isEmpty() )
            return opt;

        Survey survey = opt.get();
        if( !survey.getOwnerId().equals(ownerId) ) {
            log.warn("[IMPORTANT] Intercept unauthorized access to survey: {} from: {}", id, ownerId);
            throw new AccessDeniedException("Access denied to survey: "+id);
        }
        return Optional.of(survey);
    }

    /** Trova tutte le survey pubblicate da un utente */
    public List<Survey> findAllByOwnerId(String ownerId) {
        return execute(
            () ->   firestore.collection("surveys")
                                .whereEqualTo("ownerId", ownerId)
                                .get()
                                .get()
                                .getDocuments()
                                .stream()
                                .map(doc -> {
                                    Survey s = doc.toObject(Survey.class);
                                    checkAndCloseIfExpired(s);
                                    return s;
                                })
                                .toList(),
                    String.format("finding surveys for owner %s", ownerId)
        );
    }

    /** Trova tutte le survey a cui 'email' può rispondere o ha già risposto */
    public List<Survey> findAllWhereUserIsInvited(String email) {
        return execute(
                () -> firestore.collection("surveys")
                                .whereArrayContains("invitedEmails", email)
                                .get()
                                .get()
                                .getDocuments()
                                .stream()
                                .map(doc -> {
                                    Survey s = doc.toObject(Survey.class);
                                    checkAndCloseIfExpired(s);
                                    return s;
                                })
                                .toList(),
                String.format("finding surveys by invitedEmail %s", email)
        );
    }

    /** Trova tutte le survey a cui 'user.uid' ha già risposto */
    public List<Survey> findAllWithSubmissionByUser(String uid) {
        return execute(
                () -> {
                    List<QueryDocumentSnapshot> documents = firestore.collection("surveys")
                            .get()
                            .get()
                            .getDocuments();

                    List<Survey> result = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : documents) {
                        DocumentReference submissionRef = doc.getReference()
                                .collection("submissions")
                                .document(uid);

                        if (submissionRef.get().get().exists()) {
                            Survey s = doc.toObject(Survey.class);
                            checkAndCloseIfExpired(s);
                            result.add(s);
                        }
                    }

                    return result;
                },
                String.format("finding surveys with submission by uid %s", uid)
        );
    }


    /** Trova tutte le survey a cui 'email' può rispondere o ha già risposto */
    public List<Survey> findAllWhereUserIsInvitedAndIsOpen(String email) {
        return execute(
                () -> firestore.collection("surveys")
                        .whereEqualTo("status", Status.OPEN)
                        .whereArrayContains("invitedEmails", email)
                        .get()
                        .get()
                        .getDocuments()
                        .stream()
                        .map(doc -> {
                            Survey s = doc.toObject(Survey.class);
                            checkAndCloseIfExpired(s);
                            return s;
                        })
                        .toList(),
                String.format("finding surveys by invitedEmail %s", email)
        );
    }

    /** Trova tutte le survey publiche */
    public List<Survey> findAllWithPublicVisibility() {
        return execute(
                () -> firestore.collection("surveys")
                                .whereEqualTo("visibility", "public")
                                .get()
                                .get()
                                .getDocuments()
                                .stream()
                                .map(doc -> {
                                    Survey s = doc.toObject(Survey.class);
                                    checkAndCloseIfExpired(s);
                                    return s;
                                })
                                .toList(),
                "finding surveys with visibility: 'public'"
        );
    }

    /** Trova la survey associata a quel token */
    public Optional<Survey> findByToken(String token) {
        return execute(
                () ->   {
                    QuerySnapshot snap = firestore.collection("surveys")
                            .whereEqualTo("invitedToken", token)
                            .get()
                            .get();

                    if( snap.isEmpty() )
                        return Optional.empty();

                    Survey s = snap.getDocuments().get(0).toObject(Survey.class);
                    checkAndCloseIfExpired(s);
                    return Optional.of(s);
                },
                String.format("finding surveys with token: %s", token)
        );
    }

    /* Location Saved: "surveys/{surveyId}/questions/{id}" */
    public Survey save(Survey s) {
        execute(
            () ->   firestore.collection("surveys")
                                .document(s.getId())
                                .set(s)
                                .get(),
                    String.format("saving survey %s", s.getId())
        );
        return s;
    }

    /** Elimina una survey per ID. */
    public void deleteById(String id) {
        execute(
            () ->   firestore.collection("surveys")
                                .document(id)
                                .delete()
                                .get(),
                    String.format("deleting survey %s", id)
        );
    }

    private void checkAndCloseIfExpired(Survey survey) {
        if( survey==null || survey.getStatus()!=Status.OPEN )
            return;

        if( survey.getExpirationDate().isBefore(DateUtils.now()) ) {
            survey.setStatus(Status.CLOSED);
            save(survey);
            log.info("[SurveyDao] Survey {} is expired and automatically closed.", survey.getId());
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private <T> T execute(Callable<T> action, String operation) {
        try {
            return action.call();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("[Firestore][surveyDao] Interrupted {}", operation, ie);
            throw new FirestoreIOException(ERROR_MESSAGE, ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            log.error("[Firestore][surveyDao] Error {}: {}", operation, cause == null ? ee.getMessage() : cause.getMessage(), ee);
            throw new FirestoreIOException(ERROR_MESSAGE, cause != null ? cause : ee);
        } catch (Exception ex) {
            log.error("[Firestore][surveyDao] Unexpected error {}: {}", operation, ex.getMessage(), ex);
            throw new FirestoreIOException(ERROR_MESSAGE, ex);
        }
    }

}

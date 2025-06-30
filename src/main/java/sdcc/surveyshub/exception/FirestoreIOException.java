package sdcc.surveyshub.exception;

public class FirestoreIOException extends RuntimeException {

    public FirestoreIOException(String message) {
        super(message);
    }

    public FirestoreIOException(String message, Throwable cause) {
        super(message, cause);
    }
}

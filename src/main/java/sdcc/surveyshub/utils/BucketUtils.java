package sdcc.surveyshub.utils;

import sdcc.surveyshub.exception.BucketInjectException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BucketUtils {

    private static String bucketName;

    public static void initialize(String name) throws BucketInjectException {
        bucketName = name;
        if( bucketName == null || bucketName.isBlank() )
            throw new BucketInjectException("Bucket name not initialized in BucketUtils");
    }

    public static String surveyQuestionImgPath(String surveyId, int questionSequenceNumber) {
        return String.format("%s/IMG_Q-%03d.png", surveyId, questionSequenceNumber);
    }

    public static String surveyQuestionChoiceImgPath(String surveyId, int questionSequenceNumber, int choiceSequenceNumber) {
        return String.format("%s/IMG_Q-%03d_C-%03d.png", surveyId, questionSequenceNumber, choiceSequenceNumber);
    }

    public static String firebaseImageUrl(String objectPath) {
        return String.format( "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                                bucketName,
                                URLEncoder.encode(objectPath, StandardCharsets.UTF_8)
        );
    }
}

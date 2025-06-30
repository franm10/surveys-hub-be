package sdcc.surveyshub.model.record;

import sdcc.surveyshub.model.entity.Question;

import java.util.List;
import java.util.Map;

/** Used in SurveyService.buildQuestionsAndImages */
public record QuestionsBuildResults(
        List<Question> questions,
        Map<String,String> imagesToUpload   // Map: imgBucketUrl, imgBase64
) { }
package sdcc.surveyshub.model.record.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionRequest(

        @NotNull @NotEmpty  String text,
        @NotNull            boolean allowMultipleAnswers,
        @NotNull @NotEmpty  List<QuestionChoiceRequest> options,

                            String imageType,       // "URL" | "BASE64" | null
                            String imageData        // url o base64-png

) { }

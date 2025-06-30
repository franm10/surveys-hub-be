package sdcc.surveyshub.model.record.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record QuestionChoiceRequest(

        @NotNull @NotEmpty  String text,

                            String imageType,
                            String imageData

) { }
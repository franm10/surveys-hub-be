package sdcc.surveyshub.utils;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailValidatorUtil {

    public static boolean isValidEmail(String email) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validateValue(EmailWrapper.class, "email", email).isEmpty();
        } catch (Exception e) {
            log.warn("[EmailValidator] Invalid email: {}", email);
            return false;
        }
    }

    private static class EmailWrapper {
        @SuppressWarnings("unused")
        @Email
        String email;
    }

}


package sdcc.surveyshub.model.record.response;

public record ApiResponse<T>(
        String status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> build(String status, T data) {
        return new ApiResponse<>(status, null, data);
    }

    public static <T> ApiResponse<T> build(String status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    public static <T> ApiResponse<T> build(String status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

}

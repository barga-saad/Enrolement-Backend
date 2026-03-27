package org.bcp.dlmsenrolment.excptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;
    private ApiError error;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructors for success and error responses

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("Success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("Success");
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(String errorCode, String errorMessage, String field) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("Error");
        response.setError(new ApiError(errorCode, errorMessage, field));
        return response;
    }

    // Inner class for error details
    @Data
    @AllArgsConstructor
    private static class ApiError {
        private String errorCode;
        private String errorMessage;
        private String field;
    }
}

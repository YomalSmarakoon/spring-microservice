package com.optimagrowth.license.model.dto;

import java.util.List;

public class CircuitBreakerErrorResponse {
    private Metadata metadata;
    private List<ApiErrorResponse> errors;

    public CircuitBreakerErrorResponse() {}

    public CircuitBreakerErrorResponse(Metadata metadata, List<ApiErrorResponse> errors) {
        this.metadata = metadata;
        this.errors = errors;
    }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public List<ApiErrorResponse> getErrors() { return errors; }
    public void setErrors(List<ApiErrorResponse> errors) { this.errors = errors; }

    public static class Metadata {
        private String status;

        public Metadata() {}
        public Metadata(String status) { this.status = status; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ApiErrorResponse {
        private String message;
        private String code;   // keep as String so it prints: null when unset
        private String detail;

        public ApiErrorResponse() {}

        public ApiErrorResponse(String message, String code, String detail) {
            this.message = message;
            this.code = code;
            this.detail = detail;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
}

package dev.example.legacyorders.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ApiError {
    private final String code;
    private final String message;
    private final List<String> details;

    public ApiError(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = Collections.unmodifiableList(new ArrayList<>(details));
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<String> getDetails() { return details; }
}

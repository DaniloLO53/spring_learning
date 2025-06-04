package org.example.project.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    private String resourceName;
    private String field;
    private String fieldName;
    private Long fieldId;

    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        this.resourceName = resourceName;
        this.field = field;
        this.fieldId = fieldId;
    }

    public ResourceNotFoundException(String resourceName, String field, String fieldName) {
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }
}

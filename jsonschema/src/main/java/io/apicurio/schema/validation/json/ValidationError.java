package io.apicurio.schema.validation.json;

public class ValidationError {
    
    private String description;
    private String context;

    public ValidationError() {
        //
    }

    public ValidationError(String description, String context) {
        this.setDescription(description);
        this.setContext(context);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "{context=" + context + ", description=" + description + "}";
    }

}

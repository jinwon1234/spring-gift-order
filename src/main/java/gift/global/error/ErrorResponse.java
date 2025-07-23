package gift.global.error;


import java.util.List;
import java.util.Map;

public class ErrorResponse {

    private Map<String,String> fieldErrors;
    private List<ObjectErrorResponse> objectErrors;

    public ErrorResponse(Map<String, String> fieldErrors, List<ObjectErrorResponse> objectErrors) {
        this.fieldErrors = fieldErrors;
        this.objectErrors = objectErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public List<ObjectErrorResponse> getObjectErrors() {
        return objectErrors;
    }
}

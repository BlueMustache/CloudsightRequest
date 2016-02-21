package com.example.kushal.cloudsightrequest;

/**
 * Created by Kushal on 2/21/2016.
 */
public enum CloudSightRequestStatusCode {
    FILE_UPLOAD_SUCCESS(100),
    FILE_UPLOAD_PROGRESS(102),
    FILE_UPLOAD_FAILURE(104),
    RECOGNITION_SUCCESS(200),
    RECOGNITION_PROGRESS(202),
    RECOGNITION_ERROR(204),
    RECOGNITION_TIMEOUT(206);

    private final int statusCode;
    private String success_message;
    private String error_message;

    CloudSightRequestStatusCode(int i) {
        this.statusCode = i;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLatestSuccessMessage() {
        return success_message;
    }

    public void setLatestSuccessMessage(String success_message) {
        this.success_message = success_message;
    }

    public String getLatestErrorMessage() {
        return error_message;
    }

    public void setLatestErrorMessage(String error_message) {
        this.error_message = error_message;
    }
}

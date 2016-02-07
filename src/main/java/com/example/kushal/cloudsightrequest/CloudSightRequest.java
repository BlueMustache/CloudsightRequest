package com.example.kushal.cloudsightrequest;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kushal on 2/7/2016.
 */
public class CloudSightRequest {
    private static DataManager manager = new DataManager();
    private ResponseCallback<CloudSightUploadResponse> uploadResponse;
    private ResponseCallback<CloudSightRecognitionResponse> recognitionResponse;
    private DataManager.CloudSightUpload.ProgressListener progressListener;
    private CloudSightRequestModel model;
    private Timer pollingTimer;
    private long delay;
    private final ResponseCallback<CloudSightRecognitionResponse> internalRecognitionResponse =
            new ResponseCallback<CloudSightRecognitionResponse>(CloudSightRecognitionResponse.class) {
                @Override
                protected void onSuccess(CloudSightRecognitionResponse object) {
                    if (recognitionResponse != null) {
                        recognitionResponse.onSuccess(object);
                    }
                    if (object.notCompleted()) {
                        delay = TimeUnit.SECONDS.toMillis(1);
                        requestRecognition();
                    } else if (object.isTimedOut()) {
                        model.setRepost();
                        requestRecognition();
                    }
                }

                @Override
                protected void onError(String message) {
                    if (recognitionResponse != null) {
                        recognitionResponse.onError(message);
                    }
                }
            };
    private final TimerTask pollingTask = new TimerTask() {
        @Override
        public void run() {
            manager.getImageResponse(model, internalRecognitionResponse);
        }
    };
    private final ResponseCallback<CloudSightUploadResponse> internalUploadResponse =
            new ResponseCallback<CloudSightUploadResponse>(CloudSightUploadResponse.class) {
                @Override
                protected void onSuccess(CloudSightUploadResponse object) {
                    if (uploadResponse != null) {
                        uploadResponse.onSuccess(object);
                    }
                    model.setToken(object.token);
                    requestRecognition();
                }

                @Override
                protected void onError(String message) {
                    if (uploadResponse != null) {
                        uploadResponse.onError(message);
                    }
                }
            };

    public CloudSightRequest(CloudSightRequestModel model) {
        this.model = model;
        pollingTimer = new Timer();
        delay = TimeUnit.SECONDS.toMillis(4);
    }

    public void setUploadResponse(ResponseCallback<CloudSightUploadResponse> uploadResponse) {
        this.uploadResponse = uploadResponse;
    }

    public void setRecognitionResponse(ResponseCallback<CloudSightRecognitionResponse> recognitionResponse) {
        this.recognitionResponse = recognitionResponse;
    }

    public void requestUpload() {
        manager.uploadImageFile(progressListener, model, internalUploadResponse);
    }

    private void requestRecognition() {
        pollingTimer.schedule(pollingTask, delay);
    }

    public void setProgressListener(DataManager.CloudSightUpload.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }
}

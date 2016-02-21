package com.example.kushal.cloudsightrequest;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kushal on 2/7/2016.
 */
public class CloudSightRequest {
    private static DataManager manager = new DataManager();
    private static Map<Integer, CloudSightRequest> requestMap = new HashMap<>();
    private final int requestNumber;
    private WeakReference<ResponseCallback<CloudSightUploadResponse>> uploadResponse;
    private WeakReference<ResponseCallback<CloudSightRecognitionResponse>> recognitionResponse;
    private DataManager.CloudSightUpload.ProgressListener progressListener;
    private CloudSightRequestModel model;
    private final Runnable pollingTask = new Runnable() {
        @Override
        public void run() {
            manager.getImageResponse(model, internalRecognitionResponse);
        }
    };
    private ScheduledThreadPoolExecutor pollingExecutor;
    private CloudSightRequestStatusCode status;
    private final ResponseCallback<CloudSightUploadResponse> internalUploadResponse =
            new ResponseCallback<CloudSightUploadResponse>(CloudSightUploadResponse.class) {
                @Override
                protected void onSuccess(CloudSightUploadResponse object) {
                    if (uploadResponse != null && uploadResponse.get() != null) {
                        uploadResponse.get().onSuccess(object);
                    }
                    status = CloudSightRequestStatusCode.FILE_UPLOAD_SUCCESS;
                    status.setLatestSuccessMessage(object.token);
                    model.setToken(object.token);
                    requestRecognition();
                }

                @Override
                protected void onError(String message) {
                    if (uploadResponse != null && uploadResponse.get() != null) {
                        uploadResponse.get().onError(message);
                    }
                    status = CloudSightRequestStatusCode.FILE_UPLOAD_FAILURE;
                    status.setLatestErrorMessage(message);
                }
            };
    private boolean retryOnTimeOutPolicy;
    private final ResponseCallback<CloudSightRecognitionResponse> internalRecognitionResponse =
            new ResponseCallback<CloudSightRecognitionResponse>(CloudSightRecognitionResponse.class) {
                @Override
                protected void onSuccess(CloudSightRecognitionResponse object) {
                    if (recognitionResponse != null && recognitionResponse.get() != null) {
                        recognitionResponse.get().onSuccess(object);
                    }
                    if (object.notCompleted()) {
                        status = CloudSightRequestStatusCode.RECOGNITION_PROGRESS;
                    } else {
                        pollingExecutor.shutdownNow();
                        if (object.isTimedOut()) {
                            status = CloudSightRequestStatusCode.RECOGNITION_TIMEOUT;
                            model.setRepost();
                            if (retryOnTimeOutPolicy) {
                                requestRecognition();
                            }
                        } else if (object.isCompleted()) {
                            status = CloudSightRequestStatusCode.RECOGNITION_SUCCESS;
                            status.setLatestSuccessMessage(object.getName());
                        } else if (object.isSkipped()) {
                            status = CloudSightRequestStatusCode.RECOGNITION_ERROR;
                            status.setLatestErrorMessage(object.getReason());
                        }
                    }
                }

                @Override
                protected void onError(String message) {
                    if (recognitionResponse != null && recognitionResponse.get() != null) {
                        recognitionResponse.get().onError(message);
                    }
                    status = CloudSightRequestStatusCode.RECOGNITION_ERROR;
                    status.setLatestErrorMessage(message);
                }
            };

    public CloudSightRequest(CloudSightRequestModel model, boolean retryOnTimeOutPolicy) {
        this.model = model;
        this.retryOnTimeOutPolicy = retryOnTimeOutPolicy;
        pollingExecutor = new ScheduledThreadPoolExecutor(1);
        pollingExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        pollingExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.requestNumber = generateRequestNumber();
        requestMap.put(requestNumber, this);
    }

    private static int generateRequestNumber() {
        int requestNumber;
        if (requestMap.size() == 0) {
            requestNumber = new Random().nextInt();
        } else {
            requestNumber = Collections.max(requestMap.keySet()) + 1;
        }
        return requestNumber;
    }

    public static CloudSightRequestStatusCode getStatus(int requestNumber) {
        CloudSightRequest request = requestMap.get(requestNumber);
        if (request != null) {
            return request.status;
        }
        return null;
    }

    public void setUploadResponse(ResponseCallback<CloudSightUploadResponse> uploadResponse) {
        this.uploadResponse = new WeakReference<>(uploadResponse);
    }

    public void setRecognitionResponse(ResponseCallback<CloudSightRecognitionResponse> recognitionResponse) {
        this.recognitionResponse = new WeakReference<>(recognitionResponse);
    }

    public int requestUpload() {
        status = CloudSightRequestStatusCode.FILE_UPLOAD_PROGRESS;
        manager.uploadImageFile(model, internalUploadResponse, progressListener);
        return requestNumber;
    }

    private void requestRecognition() {
        pollingExecutor.scheduleAtFixedRate(pollingTask, 4, 1, TimeUnit.SECONDS);
    }

    public void setProgressListener(DataManager.CloudSightUpload.ProgressListener progressListener) {
        this.progressListener = progressListener;
        manager.setProgressListener(progressListener);
    }

    public void cancel() {
        manager.cancel();
        pollingExecutor.shutdown();
        requestMap.remove(requestNumber);
    }
}

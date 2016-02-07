package com.example.kushal.cloudsightrequest;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kushal on 2/6/2016.
 */
public abstract class ResponseCallback<T> implements Callback {
    private final Class<T> responseType;
    private Gson gson;
    private boolean isRegistered;
    private Queue<T> successMessages;
    private Queue<String> failureMessages;

    public ResponseCallback(Class<T> responseType) {
        this.responseType = responseType;
        gson = new Gson();
        successMessages = new LinkedBlockingQueue<>();
        failureMessages = new LinkedBlockingQueue<>();
    }

    public void setIsRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
        if (isRegistered) {
            for (T object : successMessages) {
                onSuccess(object);
            }
            for (String message : failureMessages) {
                onError(message);
            }
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        handleOnError(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        T object = gson.fromJson(response.body().charStream(), responseType);
        if (response.isSuccessful()) {
            handleOnSuccess(object);
        } else {
            handleOnError(response.body().string());
        }
    }

    private void handleOnSuccess(T object) {
        if (isRegistered) {
            onSuccess(object);
        } else {
            successMessages.offer(object);
        }
    }

    private void handleOnError(String message) {
        if (isRegistered) {
            onError(message);
        } else {
            failureMessages.offer(message);
        }
    }

    protected abstract void onSuccess(T object);

    protected abstract void onError(String message);
}

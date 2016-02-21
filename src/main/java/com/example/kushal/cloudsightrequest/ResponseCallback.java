package com.example.kushal.cloudsightrequest;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kushal on 2/6/2016.
 */
public abstract class ResponseCallback<T> implements Callback {
    private final Class<T> responseType;
    private Gson gson;

    public ResponseCallback(Class<T> responseType) {
        this.responseType = responseType;
        gson = new Gson();
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
        onSuccess(object);
    }

    private void handleOnError(String message) {
        onError(message);
    }

    protected abstract void onSuccess(T object);

    protected abstract void onError(String message);
}

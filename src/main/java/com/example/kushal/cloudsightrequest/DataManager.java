package com.example.kushal.cloudsightrequest;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by Kushal on 1/29/2016.
 */
public class DataManager {
    private OkHttpClient client;
    private CloudSightUpload uploadBody;
    private Call call;
    public DataManager() {
        client = new OkHttpClient();
        call = null;
    }

    public void uploadImageFile(CloudSightRequestModel model,
                                Callback responseCallback,
                                CloudSightUpload.ProgressListener listener) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (model.getImage() != null) {
            uploadBody = new CloudSightUpload(MediaType.parse(model.getMediaType()), model.getImage(), listener);
            bodyBuilder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image_request[image]\""
                            + "; filename=\"" + model.getImage().getName() + "\""), uploadBody);
        }
        for (String key : model.getParams().keySet()) {
            bodyBuilder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""
                    ), RequestBody.create(null, model.getParams().get(key)));
        }
        MultipartBody body = bodyBuilder.build();

        Request request = new Request.Builder()
                .addHeader("Authorization", model.getAuthorizationHeader())
                .url(model.getUrl())
                .post(body)
                .build();

        call = client.newCall(request);
        call.enqueue(responseCallback);
    }

    public void getImageResponse(CloudSightRequestModel model, Callback callback) {
        Request request = new Request.Builder()
                .addHeader("Authorization", model.getAuthorizationHeader())
                .url(model.getUrl())
                .build();
        call = client.newCall(request);
        call.enqueue(callback);
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

    public void setProgressListener(CloudSightUpload.ProgressListener progressListener) {
        uploadBody.listener = progressListener;
    }

    public static class CloudSightUpload extends RequestBody {
        private static final long SEGMENT_SIZE = 2048;
        private final MediaType mediaType;
        private final File file;
        ProgressListener listener;

        public CloudSightUpload(MediaType mediaType, File file,
                                @Nullable ProgressListener progressListener) {
            this.mediaType = mediaType;
            this.file = file;
            this.listener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return mediaType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            try {
                source = Okio.source(file);
                long total = 0;
                long read;
                while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                    total += read;
                    sink.flush();
                    if (listener != null) {
                        listener.onProgress(total, file.length());
                    }
                }
            } finally {
                Util.closeQuietly(source);
            }
        }

        public interface ProgressListener {
            void onProgress(long bytesWritten, long maxLength);
        }
    }
}

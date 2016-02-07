package com.example.kushal.cloudsightrequest;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kushal on 2/7/2016.
 */
public class CloudSightRequestModel {
    public static final String IMAGE_REQUESTS = "image_requests";
    public static final String IMAGE_REQUEST_LANGUAGE = "image_request[language]";
    public static final String IMAGE_REQUEST_LOCALE = "image_request[locale]";
    public static final String IMAGE_REQUEST_DEVICE_ID = "image_request[device_id]";
    private static final String IMAGE_RESPONSES = "image_responses/";
    private static final String IMAGE_REQUEST_REMOTE_URL = "image_request[remote_image_url]";
    private static final String REPOST = "/repost";
    private static final String baseUrl = "https://api.cloudsightapi.com/";
    private final HashMap<String, String> params;
    private final Locale locale;
    private final String deviceId;
    private String token;
    private File image;
    private String remoteImageUrl;
    private String url;
    private CloudSightOAuth1 oAuth1;
    private boolean isRepost;

    public CloudSightRequestModel(File image, String remoteImageUrl, Locale locale,
                                  CloudSightOAuth1 oAuth1) {
        if (image == null && remoteImageUrl == null) {
            throw new IllegalArgumentException("Requires either a file or a remote image url");
        }
        this.oAuth1 = oAuth1;
        params = new HashMap<>();
        this.locale = locale;
        this.image = image;
        this.remoteImageUrl = remoteImageUrl;
        deviceId = UUID.randomUUID().toString();
        params.put(IMAGE_REQUEST_LANGUAGE, locale.getLanguage());
        params.put(IMAGE_REQUEST_LOCALE, locale.getLanguage() + "-" + locale.getCountry());
        params.put(IMAGE_REQUEST_DEVICE_ID, deviceId);
        if (remoteImageUrl != null) {
            params.put(IMAGE_REQUEST_REMOTE_URL, remoteImageUrl);
        }
        isRepost = false;
    }

    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public void addParams(String key, String value) {
        params.put(key, value);
    }

    public String getAuthorizationHeader() {
        String authorizationHeader = oAuth1.getAuthorizationHeaderWithUrl(getUrl(),
                params,
                token == null);
        return authorizationHeader;
    }

    public String getUrl() {
        if (token == null) {
            return baseUrl + IMAGE_REQUESTS;
        } else {
            return baseUrl.concat(IMAGE_RESPONSES)
                    .concat(token)
                    .concat(isRepost ? REPOST : "");
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public File getImage() {
        return image;
    }

    public void setRepost() {
        isRepost = true;
    }
}

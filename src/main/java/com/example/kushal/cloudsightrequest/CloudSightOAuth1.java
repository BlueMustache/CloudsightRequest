package com.example.kushal.cloudsightrequest;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;

import java.security.GeneralSecurityException;
import java.util.HashMap;

/**
 * Created by Kushal on 2/7/2016.
 */
public class CloudSightOAuth1 {
    private static final String VERSION = "1.0";
    private final String CONSUMER_KEY;
    private final String CONSUMER_SECRET;

    public CloudSightOAuth1(String consumer_key, String consumer_secret) {
        CONSUMER_KEY = consumer_key;
        CONSUMER_SECRET = consumer_secret;
    }


    public String getAuthorizationHeaderWithUrl(String url, HashMap<String, String> postData, boolean isPost) {
        OAuthParameters parameters = new OAuthParameters();
        parameters.consumerKey = CONSUMER_KEY;
        parameters.computeNonce();
        parameters.computeTimestamp();
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = CONSUMER_SECRET;
        parameters.signer = signer;
        parameters.version = VERSION;
        GenericUrl requestUrl = new GenericUrl(url);
        if (isPost) {
            requestUrl.putAll(postData);
        }
        try {
            parameters.computeSignature(isPost ? HttpMethods.POST : HttpMethods.GET, requestUrl);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return parameters.getAuthorizationHeader();
    }
}

package com.dueeeke.custom;

import android.app.Application;

import com.dueeeke.videoplayer.exo.ExoMediaPlayerFactory;
import com.dueeeke.videoplayer.player.VideoViewConfig;
import com.dueeeke.videoplayer.player.VideoViewManager;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainApplication extends Application {
    public static Application context;

    @Override public void onCreate() {
        super.onCreate();
        context = this;
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                .setPlayerFactory(ExoMediaPlayerFactory.create())
                .build());
        enableUnValidHttpsCertificate();
    }

    /**
     * ExoPlayer 采用 HttpUrlConnection 进行网络连接，对于未认证的 https 证书会无法连接网络
     *      通过该方法可以全局忽略 https 证书认证
     */
    public static void enableUnValidHttpsCertificate() {
        X509TrustManager xtm = new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) { }
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) { }
            @Override public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLSocketFactory sslSocketFactory = null;

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        if (sslSocketFactory != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

}

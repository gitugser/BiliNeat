package me.iacn.bilineat.net;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by iAcn on 2017/4/18
 * Emali iAcn0301@foxmail.com
 */

public class RemoteApi {

    private static RemoteApi sRemoteApi;
    private static final String BASE_URL = "http://api.iacn.me/bilineat/";

    public static RemoteApi getInstance() {
        if (sRemoteApi == null) {
            synchronized (RemoteApi.class) {
                if (sRemoteApi == null) {
                    sRemoteApi = new RemoteApi();
                }
            }
        }

        return sRemoteApi;
    }

    private RemoteApi() {
    }

    String getNewestVersion() {
        String url = BASE_URL + "neatversion";
        return getContentByHttp(url);
    }

    String getAdapterFile(String bili) {
        String url = BASE_URL + "configfile?bili=" + bili;
        return getContentByHttp(url);
    }

    private String getContentByHttp(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int len;

            while ((len = conn.getInputStream().read()) != -1) {
                stream.write(len);
            }

            return stream.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
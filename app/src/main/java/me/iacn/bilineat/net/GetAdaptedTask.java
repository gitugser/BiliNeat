package me.iacn.bilineat.net;

import android.os.AsyncTask;
import android.preference.Preference;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iAcn on 2017/5/28
 * Emali iAcn0301@foxmail.com
 */

public class GetAdaptedTask extends AsyncTask<Void, Void, String> {

    private Preference adaptedPreference;

    public GetAdaptedTask(Preference adaptedPreference) {
        this.adaptedPreference = adaptedPreference;
    }

    @Override
    protected String doInBackground(Void... params) {
        String content = RemoteApi.getInstance().getAdaptedVersion();

        try {
            JSONObject json = new JSONObject(content);
            JSONArray array = json.getJSONArray("adaptedVersion");

            List<String> temp = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                temp.add(array.getString(i));
            }

            return TextUtils.join(", ", temp);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        String summary = TextUtils.isEmpty(s) ? "暂无版本" : s;
        adaptedPreference.setSummary(summary);
    }
}
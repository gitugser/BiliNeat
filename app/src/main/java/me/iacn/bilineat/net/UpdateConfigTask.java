package me.iacn.bilineat.net;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import me.iacn.bilineat.BuildConfig;
import me.iacn.bilineat.bean.HookBean;

/**
 * Created by iAcn on 2017/4/19
 * Emali iAcn0301@foxmail.com
 */

public class UpdateConfigTask extends AsyncTask<String, Void, Boolean> {

    private Context mContext;

    public UpdateConfigTask(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String biliVersion = params[0];
        String jsonText = RemoteApi.getInstance().getAdapterFile(biliVersion);

        try {
            JSONObject json = new JSONObject(jsonText);
            int code = json.getInt("code");

            // 不是正常返回值
            if (code != 200) return null;

            HookBean bean = new HookBean();
            bean.officialVersion = json.getString("officialVersion");
            json = json.getJSONObject("hook_info");

            bean.onlineHelper = json.getString("onlineHelper");
            bean.onlineCategoryGame = json.getString("onlineCategoryGame");
            bean.onlineToolbarGame = json.getString("onlineToolbarGame");
            bean.onlineUnicomSim = json.getString("onlineUnicomSim");
            bean.onlineFoundGame = json.getString("onlineFoundGame");
            bean.onlineGameCenter = json.getString("onlineGameCenter");

            bean.foundMall = json.getString("foundMall");
            bean.themeClass = json.getString("themeClass");
            bean.indexInnerClass = json.getString("indexInnerClass");

            String filesDir = mContext.getPackageManager().getPackageInfo(
                    BuildConfig.APPLICATION_ID, 0).applicationInfo.dataDir + "/files";

            File file = new File(filesDir, bean.officialVersion);

            // 序列化 JavaBean 到 files 目录
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(bean);
            out.close();

            file.setReadable(true, false);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) Toast.makeText(mContext, "配置文件更新成功", Toast.LENGTH_SHORT).show();
    }
}
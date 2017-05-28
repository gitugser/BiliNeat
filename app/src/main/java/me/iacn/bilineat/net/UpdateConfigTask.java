package me.iacn.bilineat.net;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
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

public class UpdateConfigTask extends AsyncTask<Object, Void, Integer> {

    private static final int RESULT_NOT_NEWEST = 200;
    private static final int RESULT_UPDATE_SUCCESS = 740;
    private static final int RESULT_UPDATE_FAILED = 366;

    private Context mContext;

    public UpdateConfigTask(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected Integer doInBackground(Object... params) {
        try {
            String newestVersion = RemoteApi.getInstance().getNewestVersion();
            boolean ignoreUpgradeHint = (boolean) params[0];

            // 不是最新版本的净化
            if (!ignoreUpgradeHint && !TextUtils.equals(newestVersion,
                    mContext.getPackageManager()
                            .getPackageInfo(BuildConfig.APPLICATION_ID, 0)
                            .versionName)) return RESULT_NOT_NEWEST;

            String biliVersion = (String) params[1];
            String jsonText = RemoteApi.getInstance().getConfigFile(biliVersion);

            JSONObject json = new JSONObject(jsonText);
            int code = json.getInt("code");

            // 不是正常返回值
            if (code != 200) return RESULT_UPDATE_FAILED;

            HookBean bean = new HookBean();
            bean.officialVersion = json.getString("officialVersion");
            json = json.getJSONObject("hookInfo");

            bean.onlineHelper = json.getString("onlineHelper");
            bean.onlineCategoryGame = json.getString("onlineCategoryGame");
            bean.onlineToolbarGame = json.getString("onlineToolbarGame");
            bean.onlineUnicomSim = json.getString("onlineUnicomSim");
            bean.onlineFoundGame = json.getString("onlineFoundGame");
            bean.onlineGameCenter = json.getString("onlineGameCenter");

            bean.foundMall = json.getString("foundMall");
            bean.themeClass = json.getString("themeClass");
            bean.indexInnerClass = json.getString("indexInnerClass");

            File file = new File(mContext.getFilesDir(), "bilineat");

            if (!file.exists()) {
                boolean mkdir = file.mkdir();
                if (!mkdir) return RESULT_UPDATE_FAILED;
            }

            file = new File(file, bean.officialVersion);

            // 序列化 JavaBean 到 files 目录
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(bean);
            out.close();

            return RESULT_UPDATE_SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT_UPDATE_FAILED;
        }
    }

    @Override
    protected void onPostExecute(Integer aInteger) {
        super.onPostExecute(aInteger);
        switch (aInteger) {
            case RESULT_NOT_NEWEST:
                Toast.makeText(mContext, "请更新哔哩净化到最新版本", Toast.LENGTH_SHORT).show();
                break;

            case RESULT_UPDATE_SUCCESS:
                Toast.makeText(mContext, "哔哩净化配置文件已更新", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
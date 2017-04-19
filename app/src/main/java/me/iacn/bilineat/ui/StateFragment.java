package me.iacn.bilineat.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.iacn.bilineat.BuildConfig;
import me.iacn.bilineat.Constant;
import me.iacn.bilineat.R;
import me.iacn.bilineat.bean.HookBean;
import me.iacn.bilineat.net.OnlineApi;

/**
 * Created by iAcn on 2017/3/26
 * Emali iAcn0301@foxmail.com
 */

public class StateFragment extends Fragment {

    private static boolean sXposedRunning;

    private TextView tvBiliVersion;
    private TextView tvNeatVersion;
    private TextView tvSupported;
    private TextView tvRunning;

    private Button btnUpdateConfig;
    private TextView tvConfigVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_state, null);

        tvBiliVersion = (TextView) rootView.findViewById(R.id.tv_bili_version);
        tvNeatVersion = (TextView) rootView.findViewById(R.id.tv_neat_version);
        tvSupported = (TextView) rootView.findViewById(R.id.tv_supported);
        tvRunning = (TextView) rootView.findViewById(R.id.tv_running);

        btnUpdateConfig = (Button) rootView.findViewById(R.id.btn_update_config);
        tvConfigVersion = (TextView) rootView.findViewById(R.id.tv_config_version);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tvBiliVersion.setText(getBiliVersionName());
        tvNeatVersion.setText(BuildConfig.VERSION_NAME);
        tvSupported.setText(isSupported() ? "是" : "否");
        tvRunning.setText(sXposedRunning ? "是" : "否");

        btnUpdateConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateConfigTask(getActivity()).execute(tvBiliVersion.getText().toString());
            }
        });
    }

    private String getBiliVersionName() {
        try {
            return getActivity().getPackageManager()
                    .getPackageInfo(Constant.biliPackageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "无法获取";
        }
    }

    private boolean isSupported() {
        String biliVersion = tvBiliVersion.getText().toString();

        Set<String> set = new HashSet<>();
        Collections.addAll(set, Constant.supportVersions);

        return set.contains(biliVersion);
    }

    class UpdateConfigTask extends AsyncTask<String, Void, Boolean> {

        private Context mContext;

        public UpdateConfigTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String biliVersion = params[0];
            String jsonText = OnlineApi.getInstance().getAdapterFile(biliVersion);

            try {
                JSONObject json = new JSONObject(jsonText);
                int code = json.getInt("code");

                // 不是正常返回值
                if (code != 200) return false;

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

                // 序列化 JavaBean 到 files 目录
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
                        new File(mContext.getFilesDir(), bean.officialVersion)));
                out.writeObject(bean);
                out.close();

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
}
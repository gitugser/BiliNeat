package me.iacn.bilineat.ui;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.iacn.bilineat.BuildConfig;
import me.iacn.bilineat.Constant;
import me.iacn.bilineat.R;
import me.iacn.bilineat.net.UpdateConfigTask;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_state, null);

        tvBiliVersion = (TextView) rootView.findViewById(R.id.tv_bili_version);
        tvNeatVersion = (TextView) rootView.findViewById(R.id.tv_neat_version);
        tvSupported = (TextView) rootView.findViewById(R.id.tv_supported);
        tvRunning = (TextView) rootView.findViewById(R.id.tv_running);

        btnUpdateConfig = (Button) rootView.findViewById(R.id.btn_update_config);

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
                String str = getBiliVersionName();
                if (str.equals("无法获取")) {
                    Toast.makeText(getActivity(), "你似乎没有安装哔哩哔哩哦", Toast.LENGTH_SHORT).show();
                } else {
                    new UpdateConfigTask(getActivity()).execute(tvBiliVersion.getText().toString());
                }
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
}
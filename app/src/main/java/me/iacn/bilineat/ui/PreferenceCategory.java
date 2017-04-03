package me.iacn.bilineat.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.iacn.bilineat.util.ThemeHelper;

/**
 * Created by iAcn on 2017/4/3
 * Emali iAcn0301@foxmail.com
 */

public class PreferenceCategory extends android.preference.PreferenceCategory {

    public PreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(ThemeHelper.getPrimaryColor());
        }

        return view;
    }
}
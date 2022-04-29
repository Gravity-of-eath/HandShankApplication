package com.android.handshankapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.handshankapplication.R;

public abstract class TitleFragment extends BaseFragment implements View.OnClickListener {

    private TextView vTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.title_fragment, container);
        View view = inflater.inflate(getLayoutResource(), root);
        root.findViewById(R.id.back).setOnClickListener(this::onBack);
        root.findViewById(R.id.menu).setOnClickListener(this);
        vTitle = root.findViewById(R.id.title);
        vTitle.setText(getTitle());
        initView(root);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    abstract void onMenuClick();

    abstract int getTitle();

    public void onBack(View v) {
        getActivity().onBackPressed();
    }

    @Override
    public void onClick(View view) {
        onMenuClick();
    }
}

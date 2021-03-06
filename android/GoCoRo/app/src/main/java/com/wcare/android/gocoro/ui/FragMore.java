package com.wcare.android.gocoro.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wcare.android.gocoro.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by ttonway on 2016/12/12.
 */
public class FragMore extends BaseFragment {

    private Unbinder mUnbinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_more, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.btn_knowledge)
    public void gotoCoffeeKnowledges() {
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_news)
    public void gotoNews() {
//        Intent intent = new Intent(getActivity(), ActivityNewsList.class);
//        startActivity(intent);
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_buy_device)
    public void gotoBuyDevice() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse("https://abudodo.world.taobao.com/"));
//        startActivity(intent);
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_buy_bean)
    public void gotoBuyBean() {
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_store)
    public void gotoShoppingStore() {
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_about)
    public void gotoAbout() {
        Intent intent = new Intent(getActivity(), ActivityAbout.class);
        startActivity(intent);
//        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_coporate)
    public void gotoCoporate() {
//        Intent intent = new Intent(getActivity(), ActivityCoporate.class);
//        startActivity(intent);
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_facebook)
    public void gotoFacebook() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse("https://www.facebook.com/groups/gocoro/"));
//        startActivity(intent);
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_sina_weibo)
    public void gotoWeibo() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse("http://weibo.com/gocoro"));
//        startActivity(intent);
        Toast.makeText(getActivity(), R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
    }
}

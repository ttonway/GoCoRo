package com.wcare.android.gocoro.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.http.KnowledgeMessage;
import com.wcare.android.gocoro.http.ServiceFactory;
import com.wcare.android.gocoro.ui.adapter.KnowledgeAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ttonway on 2017/2/23.
 */

public class ActivityNewsList extends BaseActivity {
    private static final String TAG = ActivityNewsList.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.list)
    ListView mListView;
    @BindView(R.id.internalEmpty)
    TextView mEmptyView;

    final List<KnowledgeMessage> mKnowledges = new ArrayList<>();
    KnowledgeAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge_list);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        View header = getLayoutInflater().inflate(R.layout.list_padder, null);
        View footer = getLayoutInflater().inflate(R.layout.list_padder, null);
        mListView.addHeaderView(header);
        mListView.addFooterView(footer);
        mAdapter = new KnowledgeAdapter(this, mKnowledges);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KnowledgeMessage knowledge = (KnowledgeMessage) parent.getItemAtPosition(position);
                if (knowledge != null) {
                    Intent intent = new Intent(ActivityNewsList.this, ActivityWebView.class);
                    intent.setData(Uri.parse(knowledge.url));
                    startActivity(intent);
                }
            }
        });

        Call<List<KnowledgeMessage>> call = ServiceFactory.getWebService().listKnowledgeMessages();
        call.enqueue(new Callback<List<KnowledgeMessage>>() {
            @Override
            public void onResponse(Call<List<KnowledgeMessage>> call, Response<List<KnowledgeMessage>> response) {
                if (response.isSuccessful()) {
                    List<KnowledgeMessage> list = response.body();
                    Log.d(TAG, "onResponse: " + list);

                    mAdapter.addAll(list);
                } else {
                    try {
                        Log.e(TAG, "onResponse error: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse error.", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<KnowledgeMessage>> call, Throwable t) {
                Log.e(TAG, "onFailure.", t);
            }
        });

    }
}

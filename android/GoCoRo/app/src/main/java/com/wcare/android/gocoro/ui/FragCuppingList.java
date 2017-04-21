package com.wcare.android.gocoro.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.Cupping;
import com.wcare.android.gocoro.ui.adapter.CuppingAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ttonway on 2016/12/12.
 */
public class FragCuppingList extends BaseFragment {

    public static FragCuppingList newFragment(String profileUuid, boolean internalToobar) {
        FragCuppingList frag = new FragCuppingList();
        Bundle args = new Bundle();
        args.putString("profileUuid", profileUuid);
        args.putBoolean("internalToobar", internalToobar);
        frag.setArguments(args);
        return frag;
    }

    Realm mRealm;
    RealmResults<Cupping> mCuppings;
    String mProfileUuid;
    boolean mInternalToobar;

    CuppingAdapter mAdapter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.text_title)
    TextView mTitleTextView;
    @BindView(R.id.list)
    ListView mListView;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mInternalToobar = args.getBoolean("internalToobar", true);
            mProfileUuid = args.getString("profileUuid");
        } else {
            mInternalToobar = true;
        }
        mRealm = Realm.getDefaultInstance();
        RealmQuery<Cupping> query = mRealm.where(Cupping.class);
        if (!TextUtils.isEmpty(mProfileUuid)) {
            query.equalTo("profile.uuid", mProfileUuid);
        }
        mCuppings = query.findAll().sort("time", Sort.DESCENDING);
        mAdapter = new CuppingAdapter(getActivity(), mCuppings);
        mCuppings.addChangeListener(new RealmChangeListener<RealmResults<Cupping>>() {
            @Override
            public void onChange(RealmResults<Cupping> results) {
                mAdapter.notifyDataSetChanged();
            }
        });

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_toolbar_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        if (mInternalToobar) {
            getBaseActivity().setSupportActionBar(mToolbar);
            getBaseActivity().getSupportActionBar().setTitle("");
            mTitleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_cupping, 0, 0, 0);
            mTitleTextView.setText(R.string.activity_cupping_list);
        } else {
            mToolbar.setVisibility(View.GONE);
        }


        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cupping item = (Cupping) parent.getItemAtPosition(position);
                if (item != null) {
                    ActivityCupping.viewCupping(getActivity(), item.getUuid());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mInternalToobar) {
            getBaseActivity().setSupportActionBar(null);
        }
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cupping_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                ActivityCupping.startCupping(getActivity(), mProfileUuid);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

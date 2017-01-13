package com.wcare.android.gocoro.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.ActivityCuppingList;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by ttonway on 2016/12/19.
 */
public class ProfileAdapter extends BaseSwipeAdapter {

    Context mContext;
    LayoutInflater mInflater;
    DateFormat mDateFormat;

    List<RoastProfile> mData;

    public ProfileAdapter(Context context, List<RoastProfile> objects) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDateFormat = DateFormat.getDateTimeInstance();
        mData = objects;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RoastProfile getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.list_item_profile_actions, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void fillValues(int position, View convertView) {
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final RoastProfile item = getItem(position);
        holder.name.setText(item.getFullName());
        holder.weight.setText((int) item.getStartWeight() + "g");
        holder.time.setText(mDateFormat.format(new Date(item.getStartTime())));

        holder.btnCupping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCuppingList.viewCuppingList(mContext, item.getUuid());
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.swipeLayout.close(false);

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        item.deleteFromRealm();
                    }
                });
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.swipe_layout) SwipeLayout swipeLayout;
        @BindView(R.id.text1) TextView name;
        @BindView(R.id.text2) TextView weight;
        @BindView(R.id.text3) TextView time;
        @BindView(R.id.btn_cupping) Button btnCupping;
        @BindView(R.id.btn_delete) Button btnDelete;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

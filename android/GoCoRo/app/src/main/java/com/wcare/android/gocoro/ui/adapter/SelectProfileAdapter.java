package com.wcare.android.gocoro.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttonway on 2016/12/19.
 */
public class SelectProfileAdapter extends ArrayAdapter<RoastProfile> {

    Context mContext;
    LayoutInflater mInflater;
    DateFormat mDateFormat;

    RoastProfile mSelectedProfile;

    public SelectProfileAdapter(Context context, List<RoastProfile> objects) {
        super(context, R.layout.list_item_profile_actions, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDateFormat = DateFormat.getDateTimeInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.list_item_profile, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        final RoastProfile item = getItem(position);
        holder.name.setText(item.getFullName());
        holder.weight.setText((int) item.getStartWeight() + "g");
        holder.time.setText(mDateFormat.format(new Date(item.getStartTime())));
        holder.indicator.setVisibility(item.equals(mSelectedProfile) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    public RoastProfile getSelectedProfile() {
        return mSelectedProfile;
    }

    public RoastProfile clickProfile(RoastProfile profile) {
        if (mSelectedProfile != null && mSelectedProfile.equals(profile)) {
            mSelectedProfile = null;
        } else {
            mSelectedProfile = profile;
        }
        notifyDataSetChanged();

        return mSelectedProfile;
    }

    static class ViewHolder {
        @BindView(R.id.text1) TextView name;
        @BindView(R.id.text2) TextView weight;
        @BindView(R.id.text3) TextView time;
        @BindView(R.id.indicator)
        View indicator;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

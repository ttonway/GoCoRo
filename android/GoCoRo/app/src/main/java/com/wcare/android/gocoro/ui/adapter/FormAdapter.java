package com.wcare.android.gocoro.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttonway on 2016/12/20.
 */
public class FormAdapter extends BaseExpandableListAdapter {

    List<RoastData> mPreheatData = new ArrayList<>();
    List<RoastData> mRoastData = new ArrayList<>();
    List<RoastData> mCoolData = new ArrayList<>();
    int mRoastStartTime = -1;
    int mCoolStartTime = -1;
    List<RoastData> mFireChangedData = new ArrayList<>();

    Context mContext;
    LayoutInflater mInflater;

    public FormAdapter(Context context, RoastProfile profile) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mRoastStartTime = profile.getRoastTime();
        mCoolStartTime = profile.getCoolTime();

        boolean hasEvents;
        int lastFire = -1;
        for (RoastData entry : profile.plotDatas) {
            boolean fireChanged = false;
            if (entry.getStatus() == RoastData.STATUS_ROASTING) {
                if (lastFire != -1 && lastFire != entry.getFire()) {
                    fireChanged = true;
                    mFireChangedData.add(entry);
                }
                lastFire = entry.getFire();
            }

            hasEvents = entry.getEvent() != null ||
                    fireChanged ||
                    entry.isManualCool() ||
                    entry.isCoolStatusComplete();

            int time = entry.getTime();
            if (entry.getStatus() == RoastData.STATUS_PREHEATING) {
                hasEvents = false;
            } else if (entry.getStatus() == RoastData.STATUS_ROASTING) {
                time = time - mRoastStartTime;
            } else if (entry.getStatus() == RoastData.STATUS_COOLING) {
                time = time - mCoolStartTime;
            }

            if (hasEvents || (time > 0 && time % 60 == 0)) {
                // add this entry
            } else {
                continue;
            }

            if (entry.getStatus() == RoastData.STATUS_PREHEATING) {
                mPreheatData.add(entry);
            } else if (entry.getStatus() == RoastData.STATUS_ROASTING) {
                mRoastData.add(entry);
            } else if (entry.getStatus() == RoastData.STATUS_COOLING) {
                mCoolData.add(entry);
            }
        }
    }

    @Override
    public int getGroupCount() {
        return 3;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<RoastData> group = getGroup(groupPosition);
        return group.size();
    }

    @Override
    public List<RoastData> getGroup(int groupPosition) {
        switch (groupPosition) {
            case 0:
                return mPreheatData;
            case 1:
                return mRoastData;
            case 2:
                return mCoolData;
        }
        return null;
    }

    @Override
    public RoastData getChild(int groupPosition, int childPosition) {
        List<RoastData> group = getGroup(groupPosition);
        return group.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_form_group, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.text1);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image1);

        switch (groupPosition) {
            case 0:
                textView.setText(R.string.category_preheat);
                break;
            case 1:
                textView.setText(R.string.category_roast);
                break;
            case 2:
                textView.setText(R.string.category_cool);
                break;
        }

        imageView.setBackgroundResource(isExpanded ? R.drawable.expander_open : R.drawable.expander_close);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_form_child, parent, false);
        }

        TextView text1 = (TextView) convertView.findViewById(R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(R.id.text2);
        TextView text3 = (TextView) convertView.findViewById(R.id.text3);
        TextView text4 = (TextView) convertView.findViewById(R.id.text4);

        RoastData data = getChild(groupPosition, childPosition);
        List<String> events = new ArrayList<>();
        if (data.getEvent() != null) {
            events.add(data.getEventName(mContext));
        }
        if (mFireChangedData.contains(data)) {
            events.add(mContext.getString(R.string.event_change_fire));
        }
        if (data.isManualCool()) {
            events.add(mContext.getString(R.string.event_cool_set));
        }
        if (data.isCoolStatusComplete()) {
            events.add(mContext.getString(R.string.event_cool_end));
        }
        if (groupPosition == 0) {
            text3.setVisibility(View.GONE);
            text4.setVisibility(View.GONE);

            text1.setText(Utils.formatTime2(data.getTime()));
            text2.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
        } else if (groupPosition == 1) {
            text3.setVisibility(View.VISIBLE);
            text4.setVisibility(View.VISIBLE);

            text1.setText(Utils.formatTime2(data.getTime() - mRoastStartTime));
            text2.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
            text3.setText(mContext.getString(R.string.label_fire_x, data.getFire()));
            text4.setText(TextUtils.join(" ", events));
        } else if (groupPosition == 2) {
            text3.setVisibility(View.VISIBLE);
            text4.setVisibility(View.GONE);

            text1.setText(Utils.formatTime2(data.getTime() - mCoolStartTime));
            text2.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
            text3.setText(TextUtils.join(" ", events));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

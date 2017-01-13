package com.wcare.android.gocoro.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttonway on 2016/12/20.
 */
public class FormAdapter extends BaseExpandableListAdapter {

    List<RoastData> mEventData = new ArrayList<>();
    List<RoastData> mPreheatData = new ArrayList<>();
    List<RoastData> mRoastData = new ArrayList<>();
    List<RoastData> mCoolData = new ArrayList<>();
    int mRoastStartMinute = -1;
    int mCoolStartMinute = -1;

    Context mContext;
    LayoutInflater mInflater;

    public FormAdapter(Context context, List<RoastData> objects) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        int minutes = 0;
        for (RoastData entry : objects) {
            if (entry.getEvent() != null) {
                mEventData.add(entry);
            }
            if (entry.getChangeFire() != -1) {
                RoastData data = new RoastData();
                data.setTime(entry.getTime());
                data.setChangeTime(entry.getChangeTime());
                data.setChangeFire(entry.getChangeFire());
                mEventData.add(data);
            }

            int m = entry.getTime() / 60;
            if (minutes == m) {
                continue;
            } else {
                minutes = m;
            }

            if (entry.getStatus() == RoastData.STATUS_PREHEATING) {
                mPreheatData.add(entry);
            } else if (entry.getStatus() == RoastData.STATUS_ROASTING) {
                if (mRoastStartMinute == -1) {
                    mRoastStartMinute = m;
                }
                mRoastData.add(entry);
            } else if (entry.getStatus() == RoastData.STATUS_COOLING) {
                if (mCoolStartMinute == -1) {
                    mCoolStartMinute = m;
                }
                mCoolData.add(entry);
            }
        }
    }

    @Override
    public int getGroupCount() {
        return 4;
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
                return mEventData;
            case 1:
                return mPreheatData;
            case 2:
                return mRoastData;
            case 3:
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
                textView.setText(R.string.category_event);
                break;
            case 1:
                textView.setText(R.string.category_preheat);
                break;
            case 2:
                textView.setText(R.string.category_roast);
                break;
            case 3:
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

        RoastData data = getChild(groupPosition, childPosition);
        if (groupPosition == 0) {
            text3.setVisibility(View.VISIBLE);
            if (data.isManaged()) {
                text1.setText(data.getEventNameResId());
                text2.setText(data.getTime() / 60 + "min");
                text3.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
            } else {
                // change event
                text1.setText(R.string.event_change_fire);
                text2.setText(data.getTime() / 60 + "min");
                text3.setText(mContext.getString(R.string.label_fire_x, data.getChangeFire()));
            }
        } else if (groupPosition == 1) {
            text3.setVisibility(View.GONE);
            text1.setText(data.getTime() / 60 + "min");
            text2.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
        } else if (groupPosition == 2) {
            text3.setVisibility(View.VISIBLE);
            text1.setText((data.getTime() / 60 - mRoastStartMinute + 1) + "min");
            text2.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
            text3.setText(mContext.getString(R.string.label_fire_x, data.getFire()));
        } else if (groupPosition == 3) {
            text3.setVisibility(View.GONE);
            text1.setText((data.getTime() / 60 - mCoolStartMinute + 1) + "min");
            text2.setText(mContext.getString(R.string.x_celsius_unit, data.getTemperature()));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

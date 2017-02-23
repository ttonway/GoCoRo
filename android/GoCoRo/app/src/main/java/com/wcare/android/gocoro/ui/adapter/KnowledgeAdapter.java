package com.wcare.android.gocoro.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.http.KnowledgeMessage;
import com.wcare.android.gocoro.model.RoastProfile;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttonway on 2017/2/23.
 */

public class KnowledgeAdapter extends ArrayAdapter<KnowledgeMessage> {

    Context mContext;
    LayoutInflater mInflater;
    DateFormat mDateFormat;

    RoastProfile mSelectedProfile;

    public KnowledgeAdapter(Context context, List<KnowledgeMessage> objects) {
        super(context, R.layout.list_item_knowledge, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDateFormat = DateFormat.getDateTimeInstance();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.list_item_knowledge, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        final KnowledgeMessage item = getItem(position);
        holder.title.setText(item.title);
        holder.description.setText(item.description);
        holder.time.setText(mDateFormat.format(new Date(item.createdAt)));
        Glide.with(mContext).load(item.posterUrl).into(holder.poster);
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.text1)
        TextView title;
        @BindView(R.id.text2)
        TextView time;
        @BindView(R.id.text3)
        TextView description;
        @BindView(R.id.image1)
        ImageView poster;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

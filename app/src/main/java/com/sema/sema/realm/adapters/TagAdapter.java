package com.sema.sema.realm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sema.sema.R;
import com.sema.sema.fragments.tab1hashtag;
import com.sema.sema.realm.models.Tag;

import java.util.ArrayList;

/**
 * Created by Shephard on 8/6/2017.
 */

public class TagAdapter extends BaseAdapter
{

    Context c;
    ArrayList<Tag> tags;

    public TagAdapter (tab1hashtag c, ArrayList<Tag> tags)
    {
      /*  this.c = c;*/
        this.tags = tags;
    }

    @Override
    public int getCount()
    {
        return tags.size();
    }

    @Override
    public Object getItem(int position)
    {
        return tags.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        if (view == null)
        {
            view = LayoutInflater.from(c).inflate(R.layout.hashtag_row,viewGroup, false);
        }

        TextView hashtag = (TextView) view.findViewById(R.id.post_hashtag);
        TextView name = (TextView) view.findViewById(R.id.post_name);
        TextView date = (TextView) view.findViewById(R.id.post_date);

        final Tag t = (Tag) this.getItem(position);

        hashtag.setText(t.getHashtag());
        name.setText(t.getName());
        date.setText(t.getDate());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(c, t.getHashtag(), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}

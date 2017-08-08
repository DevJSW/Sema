package com.sema.sema.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sema.sema.R;
import com.sema.sema.chatrooms.ChatroomActivity;
import com.sema.sema.fragments.tab1hashtag;
import com.sema.sema.models.Chat;
import com.sema.sema.realm.models.Tag;

import java.util.ArrayList;

/**
 * Created by Shephard on 8/7/2017.
 */

public class ChatAdapter extends BaseAdapter
{

    Context context;
    ArrayList<Chat> chats;

    public ChatAdapter (ChatroomActivity context, ArrayList<Chat> chats)
    {
        this.context = context;
        this.chats = chats;
    }

    @Override
    public int getCount()
    {
        return chats.size();
    }

    @Override
    public Object getItem(int position)
    {
        return chats.get(position);
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
            view = LayoutInflater.from(context).inflate(R.layout.chat_row,viewGroup, false);
        }

        TextView message = (TextView) view.findViewById(R.id.post_message);
        TextView message2 = (TextView) view.findViewById(R.id.post_message2);
        TextView name = (TextView) view.findViewById(R.id.post_name);
        TextView name2 = (TextView) view.findViewById(R.id.post_name2);
        TextView date = (TextView) view.findViewById(R.id.post_date);
        TextView date2 = (TextView) view.findViewById(R.id.post_date2);

        final Chat c = (Chat) this.getItem(position);

        message.setText(c.getMessage());
        message2.setText(c.getMessage());
        name.setText(c.getName());
        name2.setText(c.getName());
        date.setText(c.getDate());
        date2.setText(c.getDate());

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(context, c.getHashtag(), Toast.LENGTH_LONG).show();
            }
        });


        return view;
    }
}

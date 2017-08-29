package com.sema.sema.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sema.sema.R;
import com.sema.sema.models.Chat;

import java.util.List;

/**
 * Created by Shephard on 8/7/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>
{

    private List<Chat>  mCommentList;

    public ChatAdapter (List<Chat>  mCommentList)
    {
        this.mCommentList = mCommentList;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_row,parent, false);

        return new ChatViewHolder(v);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        public TextView message, message2;
        public TextView date, date2;
        public TextView name, name2;
        public ImageView avator, avator2;


        public ChatViewHolder(View itemView) {
            super(itemView);

            message = (TextView) itemView.findViewById(R.id.post_message);
            message2 = (TextView) itemView.findViewById(R.id.post_message2);
            date = (TextView) itemView.findViewById(R.id.post_date);
            date2 = (TextView) itemView.findViewById(R.id.post_date2);
            name = (TextView) itemView.findViewById(R.id.post_name);
            name2 = (TextView) itemView.findViewById(R.id.post_name2);
            avator = (ImageView) itemView.findViewById(R.id.post_image);
            avator2 = (ImageView) itemView.findViewById(R.id.post_image2);
        }
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }


    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {

        Chat c = mCommentList.get(position);
        holder.message.setText(c.getMessage());
        holder.message2.setText(c.getMessage());
        holder.date.setText(c.getDate());
        holder.date2.setText(c.getDate());
        holder.name.setText(c.getName());
        holder.name2.setText(c.getName());

    }
}


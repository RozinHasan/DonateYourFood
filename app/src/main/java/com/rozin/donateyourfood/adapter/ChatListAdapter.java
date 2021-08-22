package com.rozin.donateyourfood.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseUser;
import com.rozin.donateyourfood.R;
import com.rozin.donateyourfood.models.Conversation;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter
{
    private Context context;
    private String mUserId;
    private ArrayList<Conversation> mConvList;


    public ChatListAdapter(Context context, String userId, ArrayList<Conversation> convList) {
        this.context = context;
        this.mUserId = userId;
        this.mConvList = convList;
    }

    @Override
    public int getCount()
    {
        return mConvList.size();
    }


    @Override
    public Conversation getItem(int arg0)
    {
        return mConvList.get(arg0);
    }

    @Override
    public long getItemId(int arg0)
    {
        return arg0;
    }


    @Override
    public View getView(int pos, View v, ViewGroup arg2)
    {
        Conversation c = getItem(pos);

        if (c.getSender().equals(mUserId))
            v = LayoutInflater.from(context).inflate(R.layout.chat_item_sent, null);
        else
            v = LayoutInflater.from(context).inflate(R.layout.chat_item_rcv, null);

        TextView lbl = (TextView) v.findViewById(R.id.lbl1);
        lbl.setText(DateUtils.getRelativeDateTimeString(context, c.getDate().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0));

        lbl = (TextView) v.findViewById(R.id.lbl2);
        lbl.setText(c.getMsg());

        lbl = (TextView) v.findViewById(R.id.lbl3);
        if (c.getSender().equals(ParseUser.getCurrentUser().getUsername()))
        {
            if (c.getStatus() == Conversation.STATUS_SENT)
                lbl.setText("Delivered");
            else if (c.getStatus() == Conversation.STATUS_SENDING)
                lbl.setText("Sending...");
            else if (c.getStatus() == Conversation.STATUS_RECIEVED)
                lbl.setText("Recieved");
            else
                lbl.setText("Failed");
        }
        else
            lbl.setText("");

        return v;
    }

}
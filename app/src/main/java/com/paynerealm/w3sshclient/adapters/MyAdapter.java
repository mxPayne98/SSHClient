package com.paynerealm.w3sshclient.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paynerealm.w3sshclient.R;
import com.paynerealm.w3sshclient.models.Preference;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<Preference> userInfos;
    private Context context;

    public MyAdapter(ArrayList<Preference> userInfos, Context context) {
        this.userInfos = userInfos;
        this.context = context;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = li.inflate(viewType, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Preference u = userInfos.get(position);
        String user_name = u.getUsername();
        holder.textUserName.setText(user_name);
        String host = u.getHostName();
        holder.textHostName.setText(host);
        String con_name = u.getName();
        holder.textConnName.setText(con_name);
    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    public int getItemViewType(int position) {
        return R.layout.host_item_view;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewUser;
        TextView textConnName, textHostName, textUserName;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewUser = itemView.findViewById(R.id.image_view_user);
            textConnName = itemView.findViewById(R.id.name);
            textHostName = itemView.findViewById(R.id.host_name);
            textUserName = itemView.findViewById(R.id.user_name);
        }
    }
}
package com.fe.sdkparentapp;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationEntity> notifications;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public NotificationAdapter(List<NotificationEntity> notifications) {
        this.notifications = notifications;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, status,dattime;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            message = itemView.findViewById(R.id.message);
//            status = itemView.findViewById(R.id.status);
            dattime = itemView.findViewById(R.id.dattime);

        }
    }



    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);



        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(NotificationAdapter.ViewHolder holder, int position) {
        NotificationEntity notification = notifications.get(position);
        holder.title.setText(notification.title);
        holder.message.setText(notification.message);
//        holder.status.setText(notification.status);
        String formattedDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.getDefault())
                .format(new Date(notification.datetime));
        holder.dattime.setText(formattedDate);

//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onItemClick(notification);
//            }
//        });
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                Log.d("NotificationAdapter", "Clicked item position: " + adapterPosition);
                listener.onItemClick(notifications.get(adapterPosition));
            } else {
                Log.d("NotificationAdapter", "Listener null or invalid position");
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<NotificationEntity> newList) {
        this.notifications.clear();
        this.notifications.addAll(newList);
        notifyDataSetChanged();
    }

}

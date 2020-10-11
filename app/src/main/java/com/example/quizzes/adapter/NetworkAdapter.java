package com.example.quizzes.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.activity.core.ProfileActivity;
import com.example.quizzes.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.ViewHolder> {

    private List<User> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    String backToID;

    public NetworkAdapter(Context context, List<User> list, String backToID) {
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.backToID = backToID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.network_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.usernameTV.setText(list.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView photoIV;
        TextView usernameTV;

        View v;

        public ViewHolder(final View itemView) {
            super(itemView);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            v = itemView;
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
            v.getContext().startActivity(new Intent(v.getContext(), ProfileActivity.class)
            .putExtra(StaticClass.PROFILE_ID, list.get(getAdapterPosition()).getId())
            .putExtra(StaticClass.BACK_TO_ID, backToID));
        }
    }

    User getItem(int id) {
        return list.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

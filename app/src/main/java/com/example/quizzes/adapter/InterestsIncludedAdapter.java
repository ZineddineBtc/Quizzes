package com.example.quizzes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;

import java.util.List;

public class InterestsIncludedAdapter extends RecyclerView.Adapter<InterestsIncludedAdapter.ViewHolder> {

    private List<String> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public InterestsIncludedAdapter(Context context, List<String> list) {
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.interests_included_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.interestTV.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView interestTV;

        public ViewHolder(final View itemView) {
            super(itemView);
            interestTV = itemView.findViewById(R.id.interestTV);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    String getItem(int id) {
        return list.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

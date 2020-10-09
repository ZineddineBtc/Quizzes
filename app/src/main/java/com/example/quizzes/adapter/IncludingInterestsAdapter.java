package com.example.quizzes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;
import com.example.quizzes.activity.core.CreateQuizActivity;

import java.util.List;

public class IncludingInterestsAdapter extends RecyclerView.Adapter<IncludingInterestsAdapter.ViewHolder> {

    private List<String> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public IncludingInterestsAdapter(Context context, List<String> allInterests) {
        this.mInflater = LayoutInflater.from(context);
        this.list = allInterests;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.string_checkbox_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.interestTV.setText(list.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    CreateQuizActivity.interestsIncluded.add(list.get(position));
                }else{
                    CreateQuizActivity.interestsIncluded.remove(list.get(position));
                }
                CreateQuizActivity.interestsIncludedAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView interestTV;
        CheckBox checkBox;

        public ViewHolder(final View itemView) {
            super(itemView);
            interestTV = itemView.findViewById(R.id.interestTV);
            checkBox = itemView.findViewById(R.id.checkBox);
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

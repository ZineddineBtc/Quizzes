package com.example.quizzes.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.model.Quiz;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyQuizzesAdapter extends RecyclerView.Adapter<MyQuizzesAdapter.ViewHolder> {

    private List<Quiz> quizList;;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context context;

    public MyQuizzesAdapter(Context context, List<Quiz> quizList) {
        this.mInflater = LayoutInflater.from(context);
        this.quizList = quizList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.quiz_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.usernameTV.setText(holder.sharedPreferences.getString(StaticClass.USERNAME, "no username"));
        setPercentage(holder, position);
        setLikesCount(holder, position);
        setDislikesCount(holder, position);
        holder.descriptionTV.setText(quizList.get(position).getDescription());
        randomizeAnswer(holder, position);
        setListeners(holder, position);
        setLikedOrDisliked(holder, position);
        setRecyclerView(holder, position);
    }
    private void setPercentage(ViewHolder holder, int position){
        StringBuilder percent = new StringBuilder();
        int total = (int) (quizList.get(position).getCorrectCount() +
                           quizList.get(position).getWrongCount());
        if(total != 0) {
            int percentage = ((int)quizList.get(position).getCorrectCount())/total;
            percentage *= 100;
            percent.append(percentage).append("%");
        }else{
            percent.append("?%");
        }
        holder.percentageTV.setText(percent);
    }
    private void setLikesCount(ViewHolder holder, int position){
        int likesCount = (int) quizList.get(position).getLikesCount();
        StringBuilder likesText = new StringBuilder();
        if(likesCount>1000 && likesCount<1000000){
            likesCount = likesCount/1000;
            likesText.append(likesCount).append("K");
        }else if(likesCount>1000000){
            likesCount = likesCount/1000000;
            likesText.append(likesCount).append("M");
        }else{
            likesText.append(likesCount);
        }
        holder.likesCountTV.setText(likesText);
    }
    private void setDislikesCount(ViewHolder holder, int position){
        int dislikesCount = (int) quizList.get(position).getDislikesCount();
        StringBuilder dislikesText = new StringBuilder();
        if(dislikesCount>1000 && dislikesCount<1000000){
            dislikesCount = dislikesCount/1000;
            dislikesText.append(dislikesCount).append("K");
        }else if(dislikesCount>1000000){
            dislikesCount = dislikesCount/1000000;
            dislikesText.append(dislikesCount).append("M");
        }else{
            dislikesText.append(dislikesCount);
        }
        holder.dislikesCountTV.setText(dislikesText);
    }
    private void setListeners(final ViewHolder holder, final int position){
        holder.answer0TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { displayCorrectAnswer(holder, position, 0);
            }
        });
        holder.answer1TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { displayCorrectAnswer(holder, position, 1); }
        });
        holder.answer2TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { displayCorrectAnswer(holder, position, 2);
            }
        });
        holder.likesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { likeOnClickListener(holder, position); }
        });
        holder.dislikesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dislikeOnClickListener(holder, position); }});
    }
    private void randomizeAnswer(ViewHolder holder, int position){
        switch ((int) quizList.get(position).getCorrectIndex()){
            case 0:
                holder.answer0TV.setText(quizList.get(position).getCorrect());
                holder.answer1TV.setText(quizList.get(position).getWrong0());
                holder.answer2TV.setText(quizList.get(position).getWrong1());

                holder.answer0IV.setImageDrawable(context.getDrawable(R.drawable.ic_check_green));
                holder.answer1IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                holder.answer2IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                break;
            case 1:
                holder.answer1TV.setText(quizList.get(position).getCorrect());
                holder.answer2TV.setText(quizList.get(position).getWrong0());
                holder.answer0TV.setText(quizList.get(position).getWrong1());

                holder.answer1IV.setImageDrawable(context.getDrawable(R.drawable.ic_check_green));
                holder.answer2IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                holder.answer0IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                break;
            case 2:
                holder.answer2TV.setText(quizList.get(position).getCorrect());
                holder.answer0TV.setText(quizList.get(position).getWrong0());
                holder.answer1TV.setText(quizList.get(position).getWrong1());

                holder.answer2IV.setImageDrawable(context.getDrawable(R.drawable.ic_check_green));
                holder.answer0IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                holder.answer1IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                break;
        }
    }
    private void displayCorrectAnswer(ViewHolder holder, int position, int viewClicked){
        if (!holder.answerDisplayed) {
            holder.answer0IV.setVisibility(View.VISIBLE);
            holder.answer1IV.setVisibility(View.VISIBLE);
            holder.answer2IV.setVisibility(View.VISIBLE);
            switch ((int) quizList.get(position).getCorrectIndex()) {
                case 0:
                    holder.answer0TV.setTextColor(context.getColor(R.color.green));
                    holder.answer1TV.setTextColor(context.getColor(R.color.dark_red));
                    holder.answer2TV.setTextColor(context.getColor(R.color.dark_red));
                    break;
                case 1:
                    holder.answer1TV.setTextColor(context.getColor(R.color.green));
                    holder.answer2TV.setTextColor(context.getColor(R.color.dark_red));
                    holder.answer0TV.setTextColor(context.getColor(R.color.dark_red));
                    break;
                case 2:
                    holder.answer2TV.setTextColor(context.getColor(R.color.green));
                    holder.answer0TV.setTextColor(context.getColor(R.color.dark_red));
                    holder.answer1TV.setTextColor(context.getColor(R.color.dark_red));
                    break;
            }
            holder.answerDisplayed = true;
            setAnswered(holder, position, viewClicked);
        }
    }
    private void setAnswered(ViewHolder holder, int position, int viewClicked){
        if(!quizList.get(position).getAnswersUsers().contains(
                holder.sharedPreferences.getString(StaticClass.EMAIL, " "))){
            recordAnswer(holder, position, viewClicked);
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("answers-users", FieldValue.arrayUnion(
                            holder.sharedPreferences.getString(StaticClass.EMAIL, " ")
                    ));
        }
    }
    private void recordAnswer(ViewHolder holder, int position, int viewClicked){
        if(viewClicked==quizList.get(position).getCorrectIndex()){
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("correct-count", FieldValue.increment(1));
        }else{
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("wrong-count", FieldValue.increment(1));
        }
    }
    private void setLikedOrDisliked(ViewHolder holder, int position){
        if(quizList.get(position).getLikesUsers().contains(
                holder.sharedPreferences.getString(StaticClass.EMAIL, " "))){
            holder.likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_special));
            holder.liked = true;
        }else if(quizList.get(position).getDislikesUsers().contains(
                holder.sharedPreferences.getString(StaticClass.EMAIL, " "))){
            holder.dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_special));
            holder.disliked = true;
        }
    }
    private void setRecyclerView(ViewHolder holder, int position){
        InterestsIncludedAdapter adapter = new InterestsIncludedAdapter(context, quizList.get(position).getInterestsIncluded());
        holder.interestsIncludedRV.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false));
        holder.interestsIncludedRV.setAdapter(adapter);
    }
    private void likeOnClickListener(ViewHolder holder, int position){
        if(holder.liked){
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("likes-users", FieldValue.arrayRemove(
                            holder.sharedPreferences.getString(StaticClass.EMAIL, " ")));
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("likes-count", FieldValue.increment(-1));
            holder.likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_grey));
            quizList.get(position).setLikesCount(quizList.get(position).getLikesCount()-1);
            holder.liked = false;
        }else{
            if(holder.disliked){
                dislikeOnClickListener(holder, position);
            }
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("likes-users", FieldValue.arrayUnion(
                            holder.sharedPreferences.getString(StaticClass.EMAIL, " ")));
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("likes-count", FieldValue.increment(1));
            holder.likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_special));
            quizList.get(position).setLikesCount(quizList.get(position).getLikesCount()+1);
            holder.liked = true;
        }
        setLikesCount(holder, position);
    }
    private void dislikeOnClickListener(ViewHolder holder, int position){
        if(holder.disliked){
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("dislikes-users", FieldValue.arrayRemove(
                            holder.sharedPreferences.getString(StaticClass.EMAIL, " ")));
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("dislikes-count", FieldValue.increment(-1));
            holder.dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_grey));
            holder.dislikesCountTV.setText(
                    String.valueOf(quizList.get(position).getDislikesCount()-1));
            quizList.get(position).setDislikesCount(quizList.get(position).getDislikesCount()-1);
            holder.disliked = false;
        }else{
            if(holder.liked){
                likeOnClickListener(holder, position);
            }
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("dislikes-users", FieldValue.arrayUnion(
                            holder.sharedPreferences.getString(StaticClass.EMAIL, " ")));
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("dislikes-count", FieldValue.increment(1));
            holder.dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_special));
            quizList.get(position).setDislikesCount(quizList.get(position).getDislikesCount()+1);
            holder.disliked = true;
        }
        setDislikesCount(holder, position);
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView photoIV, answer0IV, answer1IV, answer2IV, likesIV, dislikesIV;
        TextView usernameTV, descriptionTV, answer0TV, answer1TV, answer2TV,
                 likesCountTV, dislikesCountTV, percentageTV;
        RecyclerView interestsIncludedRV;
        boolean answerDisplayed, liked, disliked;
        FirebaseFirestore database;
        SharedPreferences sharedPreferences;

        public ViewHolder(final View itemView) {
            super(itemView);
            findViewsByIds();
            database = FirebaseFirestore.getInstance();
            sharedPreferences = itemView.getContext().getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        }
        void findViewsByIds(){
            photoIV = itemView.findViewById(R.id.photoIV);
            answer0IV = itemView.findViewById(R.id.answer0IV);
            answer1IV = itemView.findViewById(R.id.answer1IV);
            answer2IV = itemView.findViewById(R.id.answer2IV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);
            answer0TV = itemView.findViewById(R.id.answer0TV);
            answer1TV = itemView.findViewById(R.id.answer1TV);
            answer2TV = itemView.findViewById(R.id.answer2TV);
            interestsIncludedRV = itemView.findViewById(R.id.includedInterestsRV);
            likesCountTV = itemView.findViewById(R.id.likesCountTV);
            dislikesCountTV = itemView.findViewById(R.id.dislikesCountTV);
            likesIV = itemView.findViewById(R.id.likesIV);
            dislikesIV = itemView.findViewById(R.id.dislikesIV);
            percentageTV = itemView.findViewById(R.id.percentageTV);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    Quiz getItem(int id) {
        return quizList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

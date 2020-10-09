package com.example.quizzes.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.activity.core.ProfileActivity;
import com.example.quizzes.model.Quiz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private List<Quiz> quizList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public TimelineAdapter(Context context, List<Quiz> quizList) {
        this.mInflater = LayoutInflater.from(context);
        this.quizList = quizList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.quiz_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setPoster(holder, position);
        setPercentage(holder, position);
        setLikesCount(holder, position);
        setDislikesCount(holder, position);
        holder.descriptionTV.setText(quizList.get(position).getDescription());
        randomizeAnswer(holder, position);
        setListeners(holder, position);
        setLikedOrDisliked(holder, position);
        setRecyclerView(holder, position);
    }
    private void setPoster(final ViewHolder holder, int position){
        holder.database.collection("users")
                .document(quizList.get(position).getPoster())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                holder.usernameTV.setText(document.get("username").toString());
                            }
                        }else{
                            Toast.makeText(context, "task is not successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        holder.usernameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile(position);
            }
        });
        holder.photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile(position);
            }
        });
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
    private void openProfile(int position){
        context.startActivity(
                new Intent(context, ProfileActivity.class)
                .putExtra(StaticClass.PROFILE_ID,
                        quizList.get(position).getPoster()));
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
        FirebaseFirestore database;
        SharedPreferences sharedPreferences;
        boolean answerDisplayed, liked, disliked;
        String username, email;
        Context context;

        public ViewHolder(final View itemView) {
            super(itemView);
            context = itemView.getContext();
            findViewsByIds();
            database = FirebaseFirestore.getInstance();
            sharedPreferences = itemView.getContext().getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            /*email = sharedPreferences.getString(StaticClass.EMAIL, " ");
            username = sharedPreferences.getString(StaticClass.USERNAME, "no username");
            usernameTV.setText(username);
            int position = getAdapterPosition();
            setPercentage(getAdapterPosition());
            setLikesCount(position);
            setDislikesCount(position);
            descriptionTV.setText(quizList.get(position).getDescription());
            randomizeAnswer(position);
            setListeners(position);
            setLikedOrDisliked(position);
            setRecyclerView(position);*/

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
        /*
        private void setPercentage(int position){
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
            percentageTV.setText(percent);
        }
        private void setLikesCount(int position){
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
            likesCountTV.setText(likesText);
        }
        private void setDislikesCount(int position){
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
            dislikesCountTV.setText(dislikesText);
        }
        private void setListeners(final int position){
            answer0TV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { displayCorrectAnswer(position, 0);
                }
            });
            answer1TV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { displayCorrectAnswer(position, 1); }
            });
            answer2TV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { displayCorrectAnswer(position, 2);
                }
            });
            likesIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { likeOnClickListener(position); }
            });
            dislikesIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { dislikeOnClickListener(position); }});
        }
        private void randomizeAnswer(int position){
            switch ((int) quizList.get(position).getCorrectIndex()){
                case 0:
                    answer0TV.setText(quizList.get(position).getCorrect());
                    answer1TV.setText(quizList.get(position).getWrong0());
                    answer2TV.setText(quizList.get(position).getWrong1());

                    answer0IV.setImageDrawable(context.getDrawable(R.drawable.ic_check_green));
                    answer1IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                    answer2IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                    break;
                case 1:
                    answer1TV.setText(quizList.get(position).getCorrect());
                    answer2TV.setText(quizList.get(position).getWrong0());
                    answer0TV.setText(quizList.get(position).getWrong1());

                    answer1IV.setImageDrawable(context.getDrawable(R.drawable.ic_check_green));
                    answer2IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                    answer0IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                    break;
                case 2:
                    answer2TV.setText(quizList.get(position).getCorrect());
                    answer0TV.setText(quizList.get(position).getWrong0());
                    answer1TV.setText(quizList.get(position).getWrong1());

                    answer2IV.setImageDrawable(context.getDrawable(R.drawable.ic_check_green));
                    answer0IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                    answer1IV.setImageDrawable(context.getDrawable(R.drawable.ic_close_dark_red));
                    break;
            }
        }
        private void displayCorrectAnswer(int position, int viewClicked){
            if (!answerDisplayed) {
                answer0IV.setVisibility(View.VISIBLE);
                answer1IV.setVisibility(View.VISIBLE);
                answer2IV.setVisibility(View.VISIBLE);
                switch ((int) quizList.get(position).getCorrectIndex()) {
                    case 0:
                        answer0TV.setTextColor(context.getColor(R.color.green));
                        answer1TV.setTextColor(context.getColor(R.color.dark_red));
                        answer2TV.setTextColor(context.getColor(R.color.dark_red));
                        break;
                    case 1:
                        answer1TV.setTextColor(context.getColor(R.color.green));
                        answer2TV.setTextColor(context.getColor(R.color.dark_red));
                        answer0TV.setTextColor(context.getColor(R.color.dark_red));
                        break;
                    case 2:
                        answer2TV.setTextColor(context.getColor(R.color.green));
                        answer0TV.setTextColor(context.getColor(R.color.dark_red));
                        answer1TV.setTextColor(context.getColor(R.color.dark_red));
                        break;
                }
                answerDisplayed = true;
                setAnswered(position, viewClicked);
            }
        }
        private void setAnswered(int position, int viewClicked){
            if(!quizList.get(position).getAnswersUsers().contains(email)){
                recordAnswer(position, viewClicked);
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("answers-users", FieldValue.arrayUnion(email));
            }
        }
        private void recordAnswer(int position, int viewClicked){
            if(viewClicked==quizList.get(position).getCorrectIndex()){
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("correct-count", FieldValue.increment(1));
            }else{
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("wrong-count", FieldValue.increment(1));
            }
        }
        private void setLikedOrDisliked(int position){
            if(quizList.get(position).getLikesUsers().contains(email)){
                likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_special));
                liked = true;
            }else if(quizList.get(position).getDislikesUsers().contains(email)){
                dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_special));
                disliked = true;
            }
        }
        private void setRecyclerView(int position){
            InterestsIncludedAdapter adapter = new InterestsIncludedAdapter(context, quizList.get(position).getInterestsIncluded());
            interestsIncludedRV.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            interestsIncludedRV.setAdapter(adapter);
        }
        private void likeOnClickListener(int position){
            if(liked){
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("likes-users", FieldValue.arrayRemove(email));
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("likes-count", FieldValue.increment(-1));
                likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_grey));
                quizList.get(position).setLikesCount(quizList.get(position).getLikesCount()-1);
                liked = false;
            }else{
                if(disliked){
                    dislikeOnClickListener(position);
                }
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("likes-users", FieldValue.arrayUnion(email));
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("likes-count", FieldValue.increment(1));
                likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_special));
                quizList.get(position).setLikesCount(quizList.get(position).getLikesCount()+1);
                liked = true;
            }
            setLikesCount(position);
        }
        private void dislikeOnClickListener(int position){
            if(disliked){
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("dislikes-users", FieldValue.arrayRemove(email));
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("dislikes-count", FieldValue.increment(-1));
                dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_grey));
                dislikesCountTV.setText(
                        String.valueOf(quizList.get(position).getDislikesCount()-1));
                quizList.get(position).setDislikesCount(quizList.get(position).getDislikesCount()-1);
                disliked = false;
            }else{
                if(liked){
                    likeOnClickListener(position);
                }
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("dislikes-users", FieldValue.arrayUnion(email));
                database.collection("quizzes")
                        .document(quizList.get(position).getId())
                        .update("dislikes-count", FieldValue.increment(1));
                dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_special));
                quizList.get(position).setDislikesCount(quizList.get(position).getDislikesCount()+1);
                disliked = true;
            }
            setDislikesCount(position);
        }
        */
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

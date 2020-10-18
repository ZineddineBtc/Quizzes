package com.example.quizzes.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private List<Quiz> quizList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private DocumentSnapshot userDocument;

    public TimelineAdapter(Context context, List<Quiz> quizList, DocumentSnapshot userDocument) {
        this.mInflater = LayoutInflater.from(context);
        this.quizList = quizList;
        this.context = context;
        this.userDocument = userDocument;
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
        setAlreadyAnswered(holder, position);
        setDescription(holder, position);
        setBookmarked(holder, position);
        setLikesCount(holder, position);
        setDislikesCount(holder, position);
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
    private void setAlreadyAnswered(ViewHolder holder, int position){
        if(quizList.get(position).getAnswersUsers().contains(
                holder.sharedPreferences.getString(StaticClass.EMAIL, " "))){
            holder.alreadyAnsweredTV.setVisibility(View.VISIBLE);
        }
    }
    private void setPercentage(ViewHolder holder, int position){
        StringBuilder percent = new StringBuilder();
        float total = (float) (quizList.get(position).getCorrectCount() +
                quizList.get(position).getWrongCount());
        if(total != 0) {
            float percentage = (quizList.get(position).getCorrectCount()/total)*100;
            percent.append(percentage).append("%");
        }else{
            percent.append("?%");
        }
        holder.percentageTV.setText(percent);
    }
    @SuppressLint("SetTextI18n")
    private void setDescription(ViewHolder holder, int position){
        if(quizList.get(position).isEdited()){
            holder.descriptionTV.setText("(Edited) "+
                    quizList.get(position).getDescription());
        }else{
            holder.descriptionTV.setText(quizList.get(position).getDescription());
        }
    }
    private void setBookmarked(ViewHolder holder, int position){
        if(holder.userBookmark != null){
            if(holder.userBookmark.contains(quizList.get(position).getId())){
                holder.bookmarked = true;
                holder.bookmarkIV.setImageDrawable(
                        context.getDrawable(R.drawable.ic_bookmark_special));
            }
        }
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
        holder.bookmarkIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBookmark(holder, position);
            }
        });
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
        boolean alreadyAnswered = false;
        if(!quizList.get(position).getAnswersUsers().contains(
                holder.sharedPreferences.getString(StaticClass.EMAIL, " "))){
            recordAnswer(holder, position, viewClicked);
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("answers-users", FieldValue.arrayUnion(
                            holder.sharedPreferences.getString(StaticClass.EMAIL, " ")
                    ));
            alreadyAnswered = true;
        }
        setResultIV(holder, position, viewClicked);
        showHardnessLL(holder, position, alreadyAnswered);
    }
    private void recordAnswer(ViewHolder holder, int position, int viewClicked){
        if(viewClicked==quizList.get(position).getCorrectIndex()){
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("correct-count", FieldValue.increment(1));
            incrementScore(holder);
        }else{
            holder.database.collection("quizzes")
                    .document(quizList.get(position).getId())
                    .update("wrong-count", FieldValue.increment(1));
        }
    }
    private void incrementScore(ViewHolder holder){
        holder.database.collection("users")
                .document(holder.sharedPreferences.getString(StaticClass.EMAIL, " "))
                .update("score", FieldValue.increment(1));
        SharedPreferences.Editor editor = holder.sharedPreferences.edit();
        editor.putLong(StaticClass.SCORE,
                (holder.sharedPreferences.getLong(StaticClass.SCORE, 0)+1));
        editor.apply();
    }
    private void setResultIV(ViewHolder holder, int position, int viewClicked){
        holder.resultIV.setImageDrawable(
                viewClicked == quizList.get(position).getCorrectIndex() ?
                        context.getDrawable(R.drawable.ic_check_green) :
                        context.getDrawable(R.drawable.ic_close_dark_red));
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
    private void showHardnessLL(final ViewHolder holder, final int position, final boolean alreadyAnswered){
        holder.hardnessLL.setVisibility(View.VISIBLE);
        holder.hardnessSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }@Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                long oldHardness = quizList.get(position).getUserDefinedHardness();
                if(!holder.oldHardnessShown) {
                    holder.hardnessTV.setText(holder.hardnessTV.getText() + " " +
                            "(Avg: " + oldHardness + ")");
                    holder.oldHardnessShown = true;
                }
                if(!alreadyAnswered) {
                    long total = quizList.get(position).getCorrectCount()+
                            quizList.get(position).getWrongCount();
                    long newHardness = (seekBar.getProgress()+oldHardness)/total;
                    holder.database.collection("quizzes")
                            .document(quizList.get(position).getId())
                            .update("hardness-user-defined", newHardness);
                }
            }
        });

    }
    private void toggleBookmark(ViewHolder holder, int position){
        String id = quizList.get(position).getId();
        holder.database.collection("users")
                .document(userDocument.getId())
                .update("bookmark", holder.bookmarked ?
                        FieldValue.arrayRemove(id) : FieldValue.arrayUnion(id));
        holder.bookmarkIV.setImageDrawable(holder.bookmarked ?
                context.getDrawable(R.drawable.ic_bookmark_grey) :
                context.getDrawable(R.drawable.ic_bookmark_special));
        holder.bookmarked = ! holder.bookmarked;
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView photoIV, answer0IV, answer1IV, answer2IV, likesIV, dislikesIV, resultIV,
                  bookmarkIV;
        TextView usernameTV, descriptionTV, answer0TV, answer1TV, answer2TV,
                likesCountTV, dislikesCountTV, percentageTV, alreadyAnsweredTV, hardnessTV;
        LinearLayout hardnessLL;
        SeekBar hardnessSB;
        RecyclerView interestsIncludedRV;
        FirebaseFirestore database;
        SharedPreferences sharedPreferences;
        boolean answerDisplayed, oldHardnessShown, liked, disliked, bookmarked;
        Context context;
        ArrayList<String> userBookmark;

        public ViewHolder(final View itemView) {
            super(itemView);
            context = itemView.getContext();
            findViewsByIds();
            database = FirebaseFirestore.getInstance();
            sharedPreferences = itemView.getContext().getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            userBookmark = (ArrayList<String>) userDocument.get("bookmark");
        }
        void findViewsByIds(){
            bookmarkIV = itemView.findViewById(R.id.bookmarkIV);
            hardnessTV = itemView.findViewById(R.id.hardnessTV);
            hardnessLL = itemView.findViewById(R.id.hardnessLL);
            hardnessSB = itemView.findViewById(R.id.hardnessSB);
            photoIV = itemView.findViewById(R.id.photoIV);
            answer0IV = itemView.findViewById(R.id.answer0IV);
            answer1IV = itemView.findViewById(R.id.answer1IV);
            answer2IV = itemView.findViewById(R.id.answer2IV);
            resultIV = itemView.findViewById(R.id.resultIV);
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
            alreadyAnsweredTV = itemView.findViewById(R.id.alreadyAnsweredTV);
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

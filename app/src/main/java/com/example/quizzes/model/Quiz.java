package com.example.quizzes.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Quiz {
    private String poster, id, description, correct, wrong0, wrong1, time;
    private long correctCount, wrongCount, likesCount, dislikesCount, correctIndex;
    private ArrayList<String> interestsIncluded, likesUsers, dislikesUsers, answersUsers;

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public ArrayList<String> getAnswersUsers() {
        return answersUsers;
    }

    public void setAnswersUsers(ArrayList<String> answersUsers) {
        this.answersUsers = answersUsers;
    }

    public ArrayList<String> getLikesUsers() {
        return likesUsers;
    }

    public void setLikesUsers(ArrayList<String> likesUsers) {
        this.likesUsers = likesUsers;
    }

    public ArrayList<String> getDislikesUsers() {
        return dislikesUsers;
    }

    public void setDislikesUsers(ArrayList<String> dislikesUsers) {
        this.dislikesUsers = dislikesUsers;
    }

    public long getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(long correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(long correctCount) {
        this.correctCount = correctCount;
    }

    public long getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(long wrongCount) {
        this.wrongCount = wrongCount;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(long dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getWrong0() {
        return wrong0;
    }

    public void setWrong0(String wrong0) {
        this.wrong0 = wrong0;
    }

    public String getWrong1() {
        return wrong1;
    }

    public void setWrong1(String wrong1) {
        this.wrong1 = wrong1;
    }

    public ArrayList<String> getInterestsIncluded() {
        return interestsIncluded;
    }

    public void setInterestsIncluded(ArrayList<String> interestsIncluded) {
        this.interestsIncluded = interestsIncluded;
    }
    public String getInterestsIncludedDAO(){
        StringBuilder s = new StringBuilder();
        for(int i=0; i<this.interestsIncluded.size()-1; i++){
            s.append(this.interestsIncluded.get(i)).append(", ");
        }
        s.append(this.interestsIncluded.get(this.interestsIncluded.size()-1));
        return s.toString();
    }
    public void setInterestsIncludedDAO(String interestsString){
        String[] temp = interestsString.split(", ");
        ArrayList<String> interests = new ArrayList<>(Arrays.asList(temp));
        setInterestsIncluded(interests);
    }
}

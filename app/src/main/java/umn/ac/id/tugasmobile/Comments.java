package umn.ac.id.tugasmobile;

public class Comments {
    public String date,comment,time,username;

    public Comments()
    {

    }

    public Comments(String date,String comment,String time,String username)
    {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }
}

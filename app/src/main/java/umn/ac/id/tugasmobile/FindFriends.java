package umn.ac.id.tugasmobile;

public class FindFriends {

    public String profileimage, fullname, desc;

    public FindFriends()
    {

    }

    public FindFriends(String profileimage, String fullname, String desc)
    {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.desc = desc;

    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

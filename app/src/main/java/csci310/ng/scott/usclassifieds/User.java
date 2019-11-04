package csci310.ng.scott.usclassifieds;

import android.net.Uri;

public class User {
    private Uri profilePic;
    private String fullName;
    private String email;
    private String phone;
    private String userID;
    private String textBio;

    public User() {}

    public User(String fullName, String email, String textBio, Uri profilePic) {
        this.profilePic = profilePic;
        this.fullName = fullName;
        this.email = email;
        this.phone = "";
        this.userID = "";
        this.textBio = textBio;
    }

    public Uri getProfilePic() {
        return profilePic;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getUserID() {
        return userID;
    }

    public String getTextBio() {
        return textBio;
    }

    public void setProfilePic(Uri profilePic) {
        this.profilePic = profilePic;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setTextBio(String textBio) {
        this.textBio = textBio;
    }
}
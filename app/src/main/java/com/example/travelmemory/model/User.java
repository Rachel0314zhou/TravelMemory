package com.example.travelmemory.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String nickname;
    private String avatarPath;
    private int age;
    private String gender;
    private String travelPreference;
    private int profileCompleted;
    private String createdAt;

    public User() {}

    public User(int id, String nickname, String avatarPath, String createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.avatarPath = avatarPath;
        this.createdAt = createdAt;
    }

    public User(int id, String username, String password, String nickname, String avatarPath, String createdAt) {
        this(id, username, password, nickname, avatarPath, 0, "", "", 0, createdAt);
    }

    public User(int id, String username, String password, String nickname, String avatarPath, int age, String gender, String travelPreference, int profileCompleted, String createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatarPath = avatarPath;
        this.age = age;
        this.gender = gender;
        this.travelPreference = travelPreference;
        this.profileCompleted = profileCompleted;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getTravelPreference() { return travelPreference; }
    public void setTravelPreference(String travelPreference) { this.travelPreference = travelPreference; }

    public int getProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(int profileCompleted) { this.profileCompleted = profileCompleted; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

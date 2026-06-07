package com.example.travelmemory.model;

public class User {
    private int id;
    private String nickname;
    private String avatarPath;
    private String createdAt;

    public User() {}

    public User(int id, String nickname, String avatarPath, String createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.avatarPath = avatarPath;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

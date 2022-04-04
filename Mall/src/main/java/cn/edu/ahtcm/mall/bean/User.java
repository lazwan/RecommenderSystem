package cn.edu.ahtcm.mall.bean;

import java.util.ArrayList;
import java.util.List;

public class User {

    // 数据库字段
    private Long id;
    private int userId;
    private String username;
    private String password;
    private int first;
    private long timestamp;

    private Double score;

    // 非数据库字段
    private List<String> prefGenres = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.userId = username.hashCode();
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int isFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean passwordMatch(String password) {
        return this.password.compareTo(password) == 0;
    }

    public List<String> getPrefGenres() {
        return prefGenres;
    }

    public void setPrefGenres(List<String> prefGenres) {
        this.prefGenres = prefGenres;
    }

    public int getFirst() {
        return first;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}

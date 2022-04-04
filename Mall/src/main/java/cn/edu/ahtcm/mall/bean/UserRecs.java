package cn.edu.ahtcm.mall.bean;

public class UserRecs {

    private int userId;
    private int recsProductId;
    private Double score;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRecsProductId() {
        return recsProductId;
    }

    public void setRecsProductId(int recsProductId) {
        this.recsProductId = recsProductId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}

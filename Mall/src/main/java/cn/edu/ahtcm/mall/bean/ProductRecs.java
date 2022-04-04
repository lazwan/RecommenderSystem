package cn.edu.ahtcm.mall.bean;

public class ProductRecs {

    private int productId;
    private int recsProductId;
    private Double score;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

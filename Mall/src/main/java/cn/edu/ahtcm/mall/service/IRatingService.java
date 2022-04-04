package cn.edu.ahtcm.mall.service;

import cn.edu.ahtcm.mall.bean.Rating;

import java.util.List;

public interface IRatingService {

    /**
     * 评分列表
     *
     * @return List<Rating>
     */
    List<Rating> getList();

    /**
     * 单个用户评分列表
     *
     * @param userId 用户 ID
     * @return List<Rating>
     */
    List<Rating> getListByUserId(int userId);

    /**
     * 根据商品 ID 和用户 ID 得到商品评分信息
     *
     * @param userId    用户 ID
     * @param productId 商品 ID
     * @return Rating
     */
    Rating getByIds(int userId, int productId);

    /**
     * 更新用户对商品的评分
     *
     * @param userId    用户 ID
     * @param productId 商品 ID
     */
    void update(int userId, int productId, double score);

    /**
     * 新的评分数据存储到数据库
     *
     * @param score     评分
     * @param userId    用户 ID
     * @param productId 商品 ID
     */
    void save(int userId, int productId, double score);
}

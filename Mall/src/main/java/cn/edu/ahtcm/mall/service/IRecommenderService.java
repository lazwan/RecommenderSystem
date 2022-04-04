package cn.edu.ahtcm.mall.service;

import cn.edu.ahtcm.mall.bean.*;

import java.util.List;

public interface IRecommenderService {

    /**
     * 评分最多商品
     *
     * @param num 取出的数量
     * @return List<RateMoreProducts> 评分最多商品列表
     */
    List<RateMoreProducts> getRateMore(int num);

    /**
     * 历史热门商品
     *
     * @param num 取出的数量
     * @return List<RateMoreRecentlyProducts> 历史热门商品列表
     */
    List<RateMoreRecentlyProducts> getRateMoreRecently(int num);

    /**
     * 离线推荐 用户推荐表
     *
     * @param userId 用户 ID
     * @return List<UserRecs> 用户推荐表列表
     */
    List<UserRecs> getUserRecsByUserId(int userId);

    /**
     * 离线推荐 商品相似度表
     *
     * @param productId 商品 ID
     * @return List<UserRecs> 商品相似度表列表
     */
    List<ProductRecs> getProductRecsByProductId(int productId);
}

package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.RateMoreRecentlyProducts;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RateMoreRecentlyProductsMapper {

    /**
     * 近期热门商品
     *
     * @param productId 商品 ID
     * @return List<RateMoreRecentlyProducts> 近期热门商品列表
     */
    List<RateMoreRecentlyProducts> getRateMoreRecently(int productId);
}

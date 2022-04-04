package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.ProductRecs;
import cn.edu.ahtcm.mall.bean.UserRecs;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecsMapper {

    /**
     * 离线推荐 用户推荐表
     *
     * @param userId 用户 ID
     * @return List<UserRecs> 用户推荐表列表
     */
    List<UserRecs> getUserRecs(int userId);

    /**
     * 离线推荐 商品相似度表
     *
     * @param productId 商品 ID
     * @return List<UserRecs> 商品相似度表列表
     */
    List<ProductRecs> getProductRecs(int productId);
}

package cn.edu.ahtcm.mall.service.impl;

import cn.edu.ahtcm.mall.bean.*;
import cn.edu.ahtcm.mall.dao.ProductMapper;
import cn.edu.ahtcm.mall.dao.RateMoreProductsMapper;
import cn.edu.ahtcm.mall.dao.RateMoreRecentlyProductsMapper;
import cn.edu.ahtcm.mall.dao.RecsMapper;
import cn.edu.ahtcm.mall.service.IRecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommenderService implements IRecommenderService {

    @Autowired
    private RateMoreProductsMapper rateMoreProductsMapper;

    @Autowired
    private RateMoreRecentlyProductsMapper rateMoreRecentlyProductsMapper;

    @Autowired
    private RecsMapper recsMapper;

    @Override
    public List<RateMoreProducts> getRateMore(int num) {
        return rateMoreProductsMapper.getRateMore(num);
    }

    @Override
    public List<RateMoreRecentlyProducts> getRateMoreRecently(int num) {
        return rateMoreRecentlyProductsMapper.getRateMoreRecently(num);
    }

    @Override
    public List<UserRecs> getUserRecsByUserId(int userId) {
        return recsMapper.getUserRecs(userId);
    }

    @Override
    public List<ProductRecs> getProductRecsByProductId(int productId) {
        return recsMapper.getProductRecs(productId);
    }
}

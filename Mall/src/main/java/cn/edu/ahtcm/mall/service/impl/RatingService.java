package cn.edu.ahtcm.mall.service.impl;

import cn.edu.ahtcm.mall.bean.Product;
import cn.edu.ahtcm.mall.bean.Rating;
import cn.edu.ahtcm.mall.dao.RatingMapper;
import cn.edu.ahtcm.mall.service.IRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class RatingService implements IRatingService {

    @Autowired
    private RatingMapper ratingMapper;

    @Override
    public List<Rating> getList() {
        return ratingMapper.getList();
    }

    @Override
    public List<Rating> getListByUserId(int userId) {
        return ratingMapper.getListByUserId(userId);
    }

    @Override
    public Rating getByIds(int userId, int productId) {
        HashMap<String, Integer> params = new HashMap<>();
        params.put("userId", userId);
        params.put("productId", productId);
        return ratingMapper.getByIds(params);
    }

    @Override
    public void update(int userId, int productId, double score) {
        Rating rating = new Rating();
        rating.setUserId(userId);
        rating.setProductId(productId);
        rating.setScore(score);
        rating.setTimestamp(System.currentTimeMillis() / 1000);
        ratingMapper.update(rating);
    }

    @Override
    public void save(int userId, int productId, double score) {
        Rating rating = new Rating();
        rating.setUserId(userId);
        rating.setProductId(productId);
        rating.setScore(score);
        rating.setTimestamp(System.currentTimeMillis() / 1000);
        ratingMapper.insert(rating);
    }


}

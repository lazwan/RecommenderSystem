package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.Rating;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface RatingMapper {

    List<Rating> getList();

    List<Rating> getListByUserId(int userId);

    Rating getByIds(HashMap<String, Integer> params);

    void insert(Rating rating);

    void update(Rating rating);
}

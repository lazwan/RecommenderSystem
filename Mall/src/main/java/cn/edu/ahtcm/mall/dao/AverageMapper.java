package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.AverageProduct;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AverageMapper {

    AverageProduct getAverage(int productId);
}

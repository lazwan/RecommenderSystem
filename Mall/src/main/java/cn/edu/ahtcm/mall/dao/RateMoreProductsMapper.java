package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.RateMoreProducts;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RateMoreProductsMapper {

    List<RateMoreProducts> getRateMore(int num);

}

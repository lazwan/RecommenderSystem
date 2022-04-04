package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> getProducts(List<Integer> ids);

    Product getProductById(int productId);
}

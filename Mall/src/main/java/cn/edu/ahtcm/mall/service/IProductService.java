package cn.edu.ahtcm.mall.service;

import cn.edu.ahtcm.mall.bean.AverageProduct;
import cn.edu.ahtcm.mall.bean.Product;

import java.util.List;

public interface IProductService {

    /**
     * 根据商品 ID 得到商品列表
     *
     * @param ids List<Integer> productIds
     * @return List<Product> 商品列表
     */
    List<Product> getProducts(List<Integer> ids);

    /**
     * 根据商品 ID 得到商品
     *
     * @param productId  productIds
     * @return Product 商品
     */
    Product getProductById(int productId);

    /**
     * 根据商品 ID 得到商品平均评分
     *
     * @param productId  productIds
     * @return AverageProduct 商品平均评分
     */
    AverageProduct getAverage(int productId);

}

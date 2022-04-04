package cn.edu.ahtcm.mall.service.impl;

import cn.edu.ahtcm.mall.bean.AverageProduct;
import cn.edu.ahtcm.mall.bean.Product;
import cn.edu.ahtcm.mall.dao.AverageMapper;
import cn.edu.ahtcm.mall.dao.ProductMapper;
import cn.edu.ahtcm.mall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AverageMapper averageMapper;

    @Override
    public List<Product> getProducts(List<Integer> ids) {
        return productMapper.getProducts(ids);
    }

    @Override
    public Product getProductById(int productId) {
        return productMapper.getProductById(productId);
    }

    @Override
    public AverageProduct getAverage(int productId) {
        return averageMapper.getAverage(productId);
    }
}

package cn.edu.ahtcm.mall.controller;

import cn.edu.ahtcm.mall.bean.Product;
import cn.edu.ahtcm.mall.bean.ProductRecs;
import cn.edu.ahtcm.mall.bean.Rating;
import cn.edu.ahtcm.mall.bean.User;
import cn.edu.ahtcm.mall.service.IProductService;
import cn.edu.ahtcm.mall.service.IRatingService;
import cn.edu.ahtcm.mall.service.IRecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private IRatingService ratingService;

    @Autowired
    private IRecommenderService recommenderService;

    @RequestMapping("/page")
    public ModelAndView page(HttpSession session, int productId) {
        ModelAndView model = new ModelAndView();
        User user = (User) session.getAttribute("user");
        List<Integer> productIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();

        try {
            // 商品信息
            Product product = productService.getProductById(productId);
            Double avg = productService.getAverage(productId).getAvg();
            BigDecimal bg = new BigDecimal(avg);

            // 平均评分信息
            avg = bg.setScale(2, RoundingMode.HALF_UP).doubleValue();
            product.setAvgScore(avg);
            model.addObject("product", product);

            // 相似推荐 itemcf
            List<ProductRecs> productRecs = recommenderService.getProductRecsByProductId(productId);
            if (productRecs.size() != 0) {
                for (ProductRecs productRec : productRecs) {
                    productIds.add(productRec.getRecsProductId());
                }
            }
            products = productService.getProducts(productIds);
            model.addObject("similarProducts", products);

            if (user != null) {
                Rating rating = ratingService.getByIds(user.getUserId(), productId);
                if (rating != null) {
                    user.setScore(rating.getScore());
                } else {
                    user.setScore(0.0);
                }
                model.addObject("user", user);
                model.setViewName("mall/item");
            } else {
                model.setViewName("item");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

}
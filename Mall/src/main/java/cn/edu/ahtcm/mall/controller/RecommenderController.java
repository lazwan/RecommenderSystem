package cn.edu.ahtcm.mall.controller;

import cn.edu.ahtcm.mall.bean.*;
import cn.edu.ahtcm.mall.service.IProductService;
import cn.edu.ahtcm.mall.service.IRecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/recommender")
public class RecommenderController {

    @Autowired
    private IRecommenderService recommenderService;

    @Autowired
    private IProductService productService;

    /**
     * 离线推荐 基于 LFM
     *
     * @param userId 用户 ID
     * @return ModelAndView
     */
    @RequestMapping("/offline")
    public ModelAndView offlineRecommender(int userId) {
        System.out.println("userId => " + userId);
        ModelAndView model = new ModelAndView();
        List<Integer> productIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        try {
            List<UserRecs> userRecs = recommenderService.getUserRecsByUserId(userId);
            if (userRecs.size() != 0) {
                for (UserRecs userRec : userRecs) {
                    productIds.add(userRec.getRecsProductId());
                }
                products = productService.getProducts(productIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addObject("offlineProducts", products);
        model.setViewName("index::offline");
        return model;
    }

    /**
     * 实时推荐
     *
     * @param userId 用户 ID
     * @return ModelAndView
     */
    @RequestMapping("/online")
    public ModelAndView onlineRecommender(int userId) {
        ModelAndView model = new ModelAndView();
        List<Integer> productIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        try {
            List<StreamRecs> streamRecs = recommenderService.getStreamRecsByUserId(userId);
            if (streamRecs.size() != 0) {
                for (StreamRecs streamRec : streamRecs) {
                    productIds.add(streamRec.getRecsProductId());
                }
                products = productService.getProducts(productIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addObject("onlineProducts", products);
        model.setViewName("index::online");
        return model;
    }

    /**
     * 热门商品，按照时间评分最多排序
     *
     * @param num 显示数量
     * @return ModelAndView
     */
    @RequestMapping("/hot")
    public ModelAndView hotRecommender(int num) {
        ModelAndView model = new ModelAndView();
        List<Integer> productIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        try {
            List<RateMoreRecentlyProducts> rateMoreRecently = recommenderService.getRateMoreRecently(num);
            for (RateMoreRecentlyProducts rateMoreRecentlyProducts : rateMoreRecently) {
                productIds.add(rateMoreRecentlyProducts.getProductId());
            }
            products = productService.getProducts(productIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addObject("hotProducts", products);
        model.setViewName("index::hot");
        return model;
    }

    /**
     * 离线统计推荐  评分最多推荐
     *
     * @param num 显示数量
     * @return ModelAndView
     */
    @RequestMapping("/statistics")
    public ModelAndView statisticsRecommender(int num) {
        ModelAndView model = new ModelAndView();
        List<Integer> productIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        try {
            List<RateMoreProducts> rateMoreProducts = recommenderService.getRateMore(num);
            for (RateMoreProducts rateMoreProduct : rateMoreProducts) {
                productIds.add(rateMoreProduct.getProductId());
            }

            products = productService.getProducts(productIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addObject("_rateMoreProducts", products);
        model.setViewName("index::statistics");
        return model;
    }

    @RequestMapping("/itemcf")
    public ModelAndView itemCFRecommender(int productId) {
        ModelAndView model = new ModelAndView();
        List<Integer> productIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        try {
            List<ItemCF> itemCF = recommenderService.getItemCFByProductId(productId);
            if (itemCF.size() != 0) {
                for (ItemCF item : itemCF) {
                    productIds.add(item.getRecsProductId());
                }
            }
            products = productService.getProducts(productIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addObject("itemcfProducts", products);
        model.setViewName("item::itemcf");
        return model;
    }
}

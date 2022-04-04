package cn.edu.ahtcm.mall.controller;

import cn.edu.ahtcm.mall.bean.Rating;
import cn.edu.ahtcm.mall.service.IRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/rating")
public class RatingController {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IRatingService ratingService;

    @RequestMapping("/markScore")
    @ResponseBody
    public String markScore(double score, int userId, int productId) {
        String log = "PRODUCT_RATING:" + userId + "|" + productId + "|" + score + "|" + System.currentTimeMillis() / 1000;
        String key = "userId:" + userId;
        String value = productId + ":" + score;
        logger.info(log);

        try {
            Rating rating = ratingService.getByIds(userId, productId);
            if (rating == null) {
                ratingService.save(userId, productId, score);
                stringRedisTemplate.opsForList().leftPush(key, value);
            } else {
                stringRedisTemplate.delete(key);
                ratingService.update(userId, productId, score);
                List<Rating> list = ratingService.getListByUserId(userId);
                for (Rating _rating : list) {
                    stringRedisTemplate.opsForList().leftPush(key, _rating.getProductId() + ":" + _rating.getScore());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return "success";
    }
}

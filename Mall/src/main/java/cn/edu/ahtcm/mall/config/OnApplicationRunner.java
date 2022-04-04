package cn.edu.ahtcm.mall.config;

import cn.edu.ahtcm.mall.bean.Rating;
import cn.edu.ahtcm.mall.service.IRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component
public class OnApplicationRunner implements ApplicationRunner {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IRatingService ratingService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("==== Rating 存入 Redis！====");
        delete();
        update();
        logger.info("==== Rating 存入成功！====");
    }

    public void delete() {
        Set<String> keys = stringRedisTemplate.keys("userId:*");
        assert keys != null;
        stringRedisTemplate.delete(keys);
    }

    public void update() {
        List<Rating> list = ratingService.getList();
        String key = "";
        String value = "";
        for (Rating rating : list) {
            key = "userId:" + rating.getUserId();
            value = rating.getProductId() + ":" + rating.getScore();
            stringRedisTemplate.opsForList().leftPush(key, value);
        }
    }
}

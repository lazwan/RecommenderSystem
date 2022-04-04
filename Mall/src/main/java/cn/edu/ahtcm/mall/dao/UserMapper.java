package cn.edu.ahtcm.mall.dao;

import cn.edu.ahtcm.mall.bean.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {

    User findByUsername(String username);

    void insert(User user);

    void update(User user);

    void deleteById(Long id);
}

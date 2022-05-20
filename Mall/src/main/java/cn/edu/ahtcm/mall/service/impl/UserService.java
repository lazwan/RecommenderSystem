package cn.edu.ahtcm.mall.service.impl;

import cn.edu.ahtcm.mall.bean.User;
import cn.edu.ahtcm.mall.dao.UserMapper;
import cn.edu.ahtcm.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        } else if (!user.passwordMatch(password)) {
            return null;
        }
        return user;
    }

    @Override
    public boolean checkUserExist(String username) {
        return null != userMapper.findByUsername(username);

    }

    @Override
    public boolean register(String username, String password) {
        User user = new User();
        user.setUserId(userMapper.getMaxId() + 1);
        user.setUsername(username);
        user.setPassword(password);
        try {
            userMapper.insert(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

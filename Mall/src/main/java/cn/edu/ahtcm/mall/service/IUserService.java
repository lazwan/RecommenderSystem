package cn.edu.ahtcm.mall.service;

import cn.edu.ahtcm.mall.bean.User;

public interface IUserService {

    User login(String username, String password);

    boolean checkUserExist(String username);

    boolean register(String username, String password);
}

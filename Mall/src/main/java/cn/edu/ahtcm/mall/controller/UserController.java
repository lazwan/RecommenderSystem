package cn.edu.ahtcm.mall.controller;

import cn.edu.ahtcm.mall.bean.User;
import cn.edu.ahtcm.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.logging.Logger;

@Controller
@RequestMapping("/user")
public class UserController {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    private IUserService userService;

    @RequestMapping("/login")
    public ModelAndView login(HttpSession session, String username, String password) {
        ModelAndView model = new ModelAndView();
        logger.info("===== login userId -> " + username + ", login password -> " + password + " =====");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            model.addObject("message", "请输入用户名和密码！");
        }
        User user = userService.login(username, password);
        if (user == null) {
            model.addObject("message", "用户名或密码错误！");
        }
        session.setAttribute("user", user);
        model.addObject("user", user);
        model.addObject("message", "success");
        model.setViewName("mall/main");
        return model;
    }

    @RequestMapping("/register")
    public ModelAndView register(@RequestParam("username") String username, @RequestParam("password") String password,
                                 @RequestParam("repassword") String repassword) {
        logger.info("===== userId -> " + username + ", password -> " + password + ", repassword ->" + repassword + " ====");
        ModelAndView model = new ModelAndView();

        if (!password.equals(repassword)) {
            model.addObject("success",false);
            model.addObject("message","两次密码不一样！");
        }
        if(userService.checkUserExist(username)){
            model.addObject("success",false);
            model.addObject("message","用户名已经被注册！");
        } else {
            model.addObject("success", userService.register(username, password));
        }
        model.setViewName("index");
        return model;
    }
}

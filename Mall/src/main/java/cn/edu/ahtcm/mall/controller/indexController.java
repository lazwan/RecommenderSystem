package cn.edu.ahtcm.mall.controller;

import cn.edu.ahtcm.mall.bean.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class indexController {

    @RequestMapping("/index")
    public ModelAndView index() {
        ModelAndView model = new ModelAndView();
        model.setViewName("index");
        return model;
    }

    @RequestMapping("/main")
    public ModelAndView main(HttpSession session) {
        ModelAndView model = new ModelAndView();
        User user = (User)session.getAttribute("user");
        model.addObject("user", user);
        model.setViewName("mall/main");
        return model;
    }
}

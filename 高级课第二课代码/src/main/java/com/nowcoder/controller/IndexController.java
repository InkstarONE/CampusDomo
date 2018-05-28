package com.nowcoder.controller;

import com.nowcoder.aspect.LogAspect;
import com.nowcoder.model.User;
import com.nowcoder.service.WendaService;
import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Response;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by nowcoder on 2016/7/10.
 */
@Controller
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

   @Autowired
   WendaService w;

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET})
    @ResponseBody
    public String index(HttpSession httpSession) {
        logger.info("中途路过");
        return w.testIOc("zhanghao");
    }

    @RequestMapping(path = {"/profile/{groupId}/{userId}"})
    @ResponseBody
    public String profile(@PathVariable("userId") int userId,
                          @PathVariable("groupId") String groupId,
                          @RequestParam(value = "type", defaultValue = "1") int type,
                          @RequestParam(value = "key", required = false) String key) {
        return String.format("Profile Page of %s / %d, t:%d k: %s", groupId, userId, type, key);
    }

    @RequestMapping(path = {"/vm"}, method = {RequestMethod.GET})
    public String template(Model model) {
        model.addAttribute("value1", "vvvvv1");
        List <String> color = Arrays.asList(new String[]{"rad","green","blue"});
        model.addAttribute("color",color);
        Map <String , String> m =new HashMap<>();
        for (int i = 0; i < 6; i++){
            m.put(String.valueOf(i),String.valueOf(i*i));
        }
        model.addAttribute("map",m);

        List<User> l = new LinkedList<>();
        for (int i= 0; i  < 6; i++){
            User u = new User(String.valueOf(i));
            l.add(u);
        }
        model.addAttribute("stuList",l);
        return "home";
    }

    @RequestMapping(path = {"/request"},method = {RequestMethod.GET})
    @ResponseBody
    public String request(HttpServletRequest request,HttpServletResponse response,HttpSession session,Model model){
        StringBuffer sb = new StringBuffer();
        sb.append(request.getPathInfo() + "<br>");
        sb.append(request.getCookies() + "<br>");
        sb.append(request.getRequestURI() + "<br>");
        sb.append(request.getRequestURL()+ "<br>");

        response.addCookie(new Cookie("usr","123"));
        sb.append(response.getHeaderNames() + "<br>");

      Enumeration<String> e =  request.getHeaderNames();
      while (e.hasMoreElements()){
          String name = e.nextElement();
          sb.append(name + "br");
      }
        return sb.toString();
    }




    @RequestMapping(path = {"/redirect/{code}"}, method = {RequestMethod.GET})
    public RedirectView redirect(@PathVariable("code") int code,
                                 HttpSession httpSession) {
        httpSession.setAttribute("msg", "jump from redirect");
        RedirectView red = new RedirectView("/", true);
        if (code == 301) {
            red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        }
        return  red;
    }

    @RequestMapping(path = {"/admin"},method = {RequestMethod.GET})
    @ResponseBody
    public String adminException(@RequestParam(value = "key",defaultValue = "admin") String key ) {
        if ("admin".equals(key)) {
            return "hello admin";
        }
        throw new IllegalArgumentException("error 参数不对");
    }


    @ExceptionHandler()
    @ResponseBody
    public String error(Exception e){
        return "home";

    }
    }

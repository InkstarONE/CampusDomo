package com.nowcoder.controller;

import com.nowcoder.service.WendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


@Controller
public class SettingController {

    @Autowired
    WendaService w;

    @RequestMapping(path = {"/set"},method = RequestMethod.GET)
    @ResponseBody
    public String setting(){
        return w.testIOc("setting lallaa");
    }
}

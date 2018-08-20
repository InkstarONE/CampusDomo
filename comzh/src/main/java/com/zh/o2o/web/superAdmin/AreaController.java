package com.zh.o2o.web.superAdmin;

import com.zh.o2o.entity.Area;
import com.zh.o2o.service.AreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/superadmin")
public class AreaController {

    Logger logger = LoggerFactory.getLogger(AreaController.class);

    @Autowired
    AreaService areaService;

    @RequestMapping(value = "/listarea",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> listArea(){
        logger.info("====start===");
        long startTime = System.currentTimeMillis();
        Map<String,Object> modelMap = new HashMap<>();
        List<Area> areaList = new ArrayList<>();

        try {
            areaList = areaService.getAreaList();
            modelMap.put("rows",areaList);
            modelMap.put("total",areaList.size());
        }catch (Exception e){
            e.printStackTrace();
            modelMap.put("sucess",false);
            modelMap.put("error",e.toString());
        }
        logger.error("test ERROR!!!");
        long endTime = System.currentTimeMillis();
        logger.debug("costTime:[{}ms]",endTime - startTime);
        logger.info("===end===");
        return modelMap;
    }
}

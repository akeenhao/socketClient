package com.sgm.robot.admin.socketpoint01.controller;

import com.sgm.robot.admin.socketpoint01.service.PointService;
import com.sgm.robot.admin.socketpoint01.entity.PointInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.Map;

/**
 * @author Cent.Chen
 * @Description
 * @date 2019/8/2 10:16 AM
 */
@Slf4j
@RestController
public class PointController {
    @Autowired
    PointService pointService;


    /**
     * 示例方法
     *
     * @return
     */
    @GetMapping
    @RequestMapping("/hello")
    public String sayHello() {
        return "Hello,This is A Service.";
    }


    @PostMapping("/getPoint")
    public PointInfo getPoint(@RequestParam(value = "point", required = false, defaultValue = "-1") String point) {
        try {
            String res = pointService.getPointInfo(point);
            log.info("point:{} info:{}", point, res);
            Map<String, String> strParams = (Map) JSON.parse(res);
            for (Map.Entry entry : strParams.entrySet()) {
                log.info("key:{} value:{}", entry.getKey(), entry.getValue());
            }

            PointInfo pointInfo = new PointInfo();
            pointInfo.setPointFlag(strParams.get("Point_flag"));
            pointInfo.setPointMessage(strParams.get("Point_message"));
            pointInfo.setPointState(strParams.get("Point_state"));
            pointInfo.setPointId(strParams.get("point_id"));
            pointInfo.setPointValue(strParams.get("point_value"));
            log.info("pointInfo:{}", pointInfo);

            return pointInfo;
        } catch (IOException e) {
            log.error("Exception", e);
            return null;
        }
    }


}

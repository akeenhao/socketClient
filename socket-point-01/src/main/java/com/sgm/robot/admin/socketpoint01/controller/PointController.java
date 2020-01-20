package com.sgm.robot.admin.socketpoint01.controller;

import com.sgm.robot.admin.socketpoint01.config.Datasources;
import com.sgm.robot.admin.socketpoint01.service.PointService;
import com.sgm.robot.admin.socketpoint01.entity.PointInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

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
    @Autowired
    Datasources datasources;

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
        Socket socket = datasources.getSocket();
        PointInfo pointInfo = new PointInfo();
        Map<String, String> resultPoint = pointService.getPointInfo(socket, point);
        String resPoint = resultPoint.get("res");
        String port = resultPoint.get("port");
        Map<String, String> strParams = (Map) JSON.parse(resPoint);

        pointInfo.setPort(port);
        pointInfo.setPointFlag(strParams.get("Point_flag"));
        pointInfo.setMessage(strParams.get("Point_message"));
        pointInfo.setFlag(strParams.get("Point_state"));
        pointInfo.setPointName(strParams.get("point_id"));
        pointInfo.setPointValue(strParams.get("point_value"));
        return pointInfo;
    }


    @PostMapping("/getPointValue")
    public String getPointValue(@RequestParam(value = "point", required = false, defaultValue = "-1") String point) {
        Socket socket = datasources.getSocket();
        PointInfo pointInfo = new PointInfo();
        Map<String, String> resultPoint = pointService.getPointInfo(socket, point);
        String resPoint = resultPoint.get("res");
        String port = resultPoint.get("port");
        Map<String, String> strParams = (Map) JSON.parse(resPoint);

        pointInfo.setPort(port);
        pointInfo.setPointFlag(strParams.get("Point_flag"));
        pointInfo.setMessage(strParams.get("Point_message"));
        pointInfo.setFlag(strParams.get("Point_state"));
        pointInfo.setPointName(strParams.get("point_id"));
        pointInfo.setPointValue(strParams.get("point_value"));
        log.info("point:{} info:{}", point, resPoint);
        log.info("pointInfo:{}", pointInfo);
        return strParams.get("point_value");
    }


    @PostMapping("/getPoints")
    public List<PointInfo> getPoints(@RequestParam(value = "points", required = false, defaultValue = "-1") String points) {
        Date startDate = new Date();
        List<PointInfo> result = new ArrayList<>();
        String[] pointStrs = points.split(",");


        int maxRowNum = pointStrs.length;

        int dealNum = 30;
        int threadNum = 4;

        ThreadGroup threadGroup = new ThreadGroup("group");
        int threadIndex = 1;

        while (true) {
//            if (threadIndex > threadNum) break;
            if ((threadIndex - 1) * dealNum >= maxRowNum) break;

            while (threadGroup.activeCount() == threadNum) {
                log.info("thread max waiting....");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("sleep exception", e);
                }
            }

            log.info("thread create:{} maxRowNum:{}", threadIndex, maxRowNum);
            int finalThreadIndex = threadIndex;
            Thread t = new Thread(threadGroup, new Runnable() {
                @Override
                public void run() {
                    Date threadStartTime = new Date();
                    int start = dealNum * (finalThreadIndex - 1) + 1;
                    int end = start + dealNum - 1;
//                    if (finalThreadIndex == threadNum) {
//                        end = maxRowNum;
//                    }
                    if (end > maxRowNum) {
                        end = maxRowNum;
                    }
                    //与服务端建立连接
                    Date getSocektStart = new Date();
                    Socket socket = datasources.getSocket();
                    log.info("socket init time:{}", new Date().getTime() - getSocektStart.getTime());

                    for (int i = start; i <= end; i++) {
                        String point = pointStrs[i - 1];
                        PointInfo pointInfo = new PointInfo();
                        pointInfo.setPointName(point);

                        Map<String, String> resultPoint = null;
                        try {
                            resultPoint = pointService.getPointInfo(socket, point);
                            log.info("read done point:{} info:{}", point, resultPoint);
                        } catch (Exception e) {
                            log.error("Exception", e);

                        }
                        pointInfo.setPointIndex(i);
                        if (null == resultPoint) {
                            pointInfo.setMessage("socket链接已用尽，请稍候");
                            result.add(pointInfo);
                            continue;
                        }
                        String resPoint = resultPoint.get("res");
                        String port = resultPoint.get("port");
                        Map<String, String> strParams = new HashMap<>();
                        try {
                            strParams = (Map) JSON.parse(resPoint);
                        } catch (Exception e) {
                            log.error("Json parse Exception", e);
                            pointInfo.setMessage("格式错误:");
                            result.add(pointInfo);
                        }

                        pointInfo.setPort(port);
//                        pointInfo.setPointFlag(strParams.get("Point_flag"));
                        pointInfo.setMessage(strParams.get("Point_message"));
                        pointInfo.setFlag(strParams.get("Point_state"));
                        pointInfo.setPointValue(strParams.get("point_value"));
//                            log.info("pointInfo:{}", pointInfo);
                        result.add(pointInfo);
                    }
                    datasources.setNotInUse(socket);
                    log.info("thread done thread:{} time:{}", finalThreadIndex, new Date().getTime() - threadStartTime.getTime());
                }
            });
            t.start();
            threadIndex++;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.error("sleep exception", e);
            }
        }

        while (threadGroup.activeCount() > 0) {
            try {
                log.info("thread dealing count:{}", threadGroup.activeCount());
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


//        for (String point : pointStrs) {
//            PointInfo pointInfo = new PointInfo();
//            try {
//                Map<String,String> resultPoint = pointService.getPointInfo(point);
//                String res = resultPoint.get("res");
//                String port = resultPoint.get("port");
//                log.info("point:{} info:{}", point, res);
//                Map<String, String> strParams = (Map) JSON.parse(res);
//                for (Map.Entry entry : strParams.entrySet()) {
//                    log.info("key:{} value:{}", entry.getKey(), entry.getValue());
//                }
//
//                pointInfo.setPointIndex(index++);
//                pointInfo.setPort(port);
//                pointInfo.setPointFlag(strParams.get("Point_flag"));
//                pointInfo.setMessage(strParams.get("Point_message"));
//                pointInfo.setFlag(strParams.get("Point_state"));
//                pointInfo.setPointName(strParams.get("point_id"));
//                pointInfo.setPointValue(strParams.get("point_value"));
//                log.info("pointInfo:{}", pointInfo);
//                result.add(pointInfo);
//            } catch (IOException e) {
//                log.error("Exception", e);
//                pointInfo.setPointName(point);
//                pointInfo.setPointValue(null);
//                pointInfo.setFlag(null);
//                pointInfo.setMessage("socket连接异常");
//                pointInfo.setPointFlag(null);
//                result.add(pointInfo);
//            }
//        }
        Date dateEnd = new Date();
        log.info("all done time:{} ", dateEnd.getTime() - startDate.getTime());
        return result;
    }

}

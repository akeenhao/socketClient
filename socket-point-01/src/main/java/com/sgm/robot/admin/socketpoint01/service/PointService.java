package com.sgm.robot.admin.socketpoint01.service;


import com.sgm.robot.admin.socketpoint01.config.Datasources;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PointService {
    @Autowired
    Datasources datasources;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Map<String, String> getPointInfo(Socket socket, String point) {
        Map<String, String> res = new HashMap<>();
        String content = "";
        Date getSocektStart = new Date();
        if (null == socket) {
            return null;
        }


        try {
            //建立连接后获取输出流
            Date getPointStart = new Date();
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            byte[] b_utf8 = point.getBytes("UTF-8");
            outputStream.write(b_utf8);
            byte[] buff = new byte[1024];
            logger.info("point:{} start reading... socketPort:{}", point, socket.getPort());
            inputStream.read(buff);
            String buffer = new String(buff, "utf-8");
            if (StringUtils.isBlank(buffer)) {
                return null;
            }
            content += buffer;
            logger.info("point:{} end reading... socketPort:{} content:{}", new Date().getTime() - getPointStart.getTime(), socket.getPort(), content);
        } catch (SocketException e) {
            logger.error("SocketException", e);
            return null;
        } catch (IOException ioe) {
            logger.error("IOException", ioe);
            return null;
        } catch (Exception e) {
            logger.error("Exception", e);
            return null;
        } finally {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                logger.error("IOException", e);
//            }
        }
//        socket.close();
        res.put("res", content);
        res.put("port", String.valueOf(socket.getPort()));
        logger.info("readPoint done point:{} socket:{} time:{} ", point, socket.getPort(), new Date().getTime() - getSocektStart.getTime());
        return res;
    }
}
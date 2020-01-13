package com.sgm.robot.admin.socketpoint01.service;


import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Service
public class PointService {

    public String getPointInfo(String point) throws IOException {
        String content = "";
        String host = "10.211.23.108";
        int port = 8080;

        //与服务端建立连接
        Socket socket = new Socket(host, port);
        socket.setOOBInline(true);

        //建立连接后获取输出流
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());

        if ("0".equals(point)) {
            return "null";
        }
        byte[] b_utf8 = point.getBytes("UTF-8");

        outputStream.write(b_utf8);
        byte[] buff = new byte[1024];
        inputStream.read(buff);
        String buffer = new String(buff, "utf-8");
        if (StringUtils.isBlank(buffer)) {
            return "null";
        }
        content += buffer;

        socket.close();
        return content;
    }
}
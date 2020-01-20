package com.sgm.robot.admin.socketpoint01.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class Datasources {
    public List<String> hostPorts = new ArrayList<>();
    public ConcurrentMap<String, List<Socket>> datasourceMap = new ConcurrentHashMap<>();
    public ConcurrentMap<Socket, Boolean> datasourceUseMap = new ConcurrentHashMap<>();
    public ConcurrentMap<Integer, Integer> datasourceBatch = new ConcurrentHashMap<>();
    public Logger logger = LoggerFactory.getLogger("Datasources");
    public int batchNum = 3;

    public Datasources() {
        List<String> hosts = new ArrayList<>();
//        hosts.add("10.211.23.108:8080");
//        hosts.add("10.211.23.108:8081");
//        hosts.add("10.211.23.108:8082");
        hosts.add("10.211.23.108:8083");
//        hosts.add("10.211.23.108:8088");

//        hosts.add("10.10.127.164:8081");
//        hosts.add("10.10.127.164:8082");
//        hosts.add("10.10.127.164:8083");
//        hosts.add("10.10.127.164:8084");
//        hosts.add("10.10.127.164:8085");
        this.hostPorts = hosts;
    }

    public synchronized boolean exist(String key) {
        return datasourceMap.containsKey(key);
    }

    synchronized void addDataSource(String key, Socket source) {

        if (null == datasourceMap.get(key)) {
            List<Socket> sockets = new ArrayList<>();
            sockets.add(source);
            datasourceMap.put(key, sockets);
        } else {
            datasourceMap.get(key).add(source);
        }
        datasourceBatch.put(source.getPort(), 1);
    }

    public synchronized void setInUse(Socket source) {
        datasourceUseMap.put(source, Boolean.TRUE);
    }

    public synchronized void setNotInUse(Socket source) {
        logger.info("set (start) not use socket:{} datasourceUseMap:{}", source, datasourceUseMap);
        if (null == source) {
            return;
        }
        datasourceUseMap.put(source, Boolean.FALSE);
        logger.info("set (end) not use socket:{} datasourceUseMap:{}", source, datasourceUseMap);
        datasourceBatch.put(source.getPort(), datasourceBatch.get(source.getPort()) - 1);
        if (datasourceBatch.get(source.getPort()) < 0) {
            datasourceBatch.put(source.getPort(), 0);
        }
    }

    public synchronized Socket getSocket() {
        Socket socket = this.getDataSource();
       /* if (null == socket) {
            logger.info("socket池已全部用尽！");
            return null;
        }*/
        if (null == socket) {
            int i = 1;
            while (true) {
                logger.info("第{}次重试建立socket", i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return null;
                }
                socket = this.getDataSource();
                if (null != socket) {
                    logger.info("waited socket:{}", socket.getPort());
                    break;
                }
                i++;
                if (i > 10) return null;
            }
        }
        try {
            socket.setOOBInline(true);
            socket.setSoTimeout(500);
            socket.setKeepAlive(true);
            setInUse(socket);
        } catch (SocketException e) {
            setNotInUse(socket);
            try {
                socket.close();
            } catch (IOException e1) {
                logger.error("close exception", e);
            }
            logger.error("keepalive exception", e);
            return null;
        }
        logger.info("socket init:{}", socket);
        return socket;
    }

    public synchronized Socket getDataSource() {
        Socket socket = null;
        for (String hostPort : hostPorts) {
            String host = hostPort.split(":")[0];
            int port = Integer.parseInt(hostPort.split(":")[1]);

            if (exist(host + port)) {
                //先找一下是否有释放的
                for (Socket socketSingle : datasourceMap.get(host + port)) {
                    logger.info("socketSingle:{} exist checkUseInfo:{}", socketSingle, datasourceUseMap);
                    if (null != datasourceUseMap && datasourceUseMap.get(socketSingle)) {
                        continue;
                    } else {
                        logger.info("socket:{} checkUseInfo not in use", socketSingle);
                        setInUse(socketSingle);
                        return socketSingle;
                    }
                }

                if (datasourceBatch.get(port) >= batchNum) {
                    continue;
                } else {
                    try {
                        socket = new Socket(host, port);
                    } catch (IOException e) {
                        logger.error("socket init error", e);
                        continue;
                    }
                    addDataSource(host+port,socket);
                    logger.info("socketPort:{} socket:{} batch num:{} batchNum:{}", socket.getPort(), socket, datasourceBatch.get(socket.getPort()), batchNum);
                    setInUse(socket);
                    return socket;
                }
            }
            try {
                logger.info("socket initing:{}:{}", host, port);
                socket = new Socket(host, port);
            } catch (IOException e) {
                logger.error("socket init error", e);
                socket = null;
                continue;
            }
            addDataSource(host + port, socket);
            setInUse(socket);
            break;
        }
//        socket.setSoTimeout();
        return socket;
    }

}

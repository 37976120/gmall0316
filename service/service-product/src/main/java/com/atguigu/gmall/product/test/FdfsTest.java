package com.atguigu.gmall.product.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class FdfsTest {
    public static void main(String[] args) throws IOException, MyException {
        String path = FdfsTest.class.getClassLoader().getResource("tracker.conf").getPath();
        ClientGlobal.init(path);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();
        //不为null就放弃负载均衡，传指定的那个
        StorageClient storageClient = new StorageClient(connection, null);
        String[] strings = storageClient.upload_file("C:/Users/hfer/Pictures/gif/a.jpg", "jpg", null);
        String url = "http://192.168.200.101";
        for (String string : strings) {
            url = url + "/" + string;
        }
        System.out.println(url);
    }
}

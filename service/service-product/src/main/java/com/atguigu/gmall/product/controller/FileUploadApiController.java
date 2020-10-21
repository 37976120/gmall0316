package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.product.test.FdfsTest;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class FileUploadApiController {

    //文件上传
    @RequestMapping("fileUpload")
    public Result<String> fileUpload(@RequestBody MultipartFile file) throws IOException, MyException {
        String path = FdfsTest.class.getClassLoader().getResource("tracker.conf").getPath();
//        path = URLDecoder.decode(path, "utf-8");//对结果编码，防止特殊字符
        ClientGlobal.init(path);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();
        int i = file.getOriginalFilename().lastIndexOf(".");
        String substring = file.getOriginalFilename().substring(i + 1);
        //不为null就放弃负载均衡，传指定的那个
        StorageClient storageClient = new StorageClient(connection, null);
        String[] strings = storageClient.upload_file(file.getBytes(), substring, null);
        String url = "http://192.168.200.128:8080";
        for (String string : strings) {
            url = url + "/" + string;
        }
        return Result.ok(url);
    }
}

package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

//    //上传文件
//    @Test
//    public void testUpload(){
//        //加载fastfafs-client,properties配置文件
//        try {
//            ClientGlobal.initByProperties("config/fastdfs-client.properties");
//            //定义TrackClient用于请求TrackerServer
//            TrackerClient trackerClient = new TrackerClient();
//            //连接Tracker
//            TrackerServer trackerServer = trackerClient.getConnection();
//            //获取Stroager
//            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
//            StorageClient1 storageClient1 =new StorageClient1(trackerServer,storeStorage);
//            //向stroager服务器上传文件
//            String filePath = "G:/logo.png";
//            //上传成功返回的文件id
//            String fileId = storageClient1.upload_file1(filePath, "png", null);
//            System.out.println(fileId);
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    //上传文件
    @Test
    public void testUpload(){

        try {
            //加载fastdfs-client.properties配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient，用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //向stroage服务器上传文件
            //本地文件的路径
            String filePath = "G:/logo.png";
            //上传成功后拿到文件Id
            String fileId = storageClient1.upload_file1(filePath, "png", null);
            System.out.println(fileId);
            //group1/M00/00/01/wKhlQVuhU3eADb4pAAAawU0ID2Q159.png
            //group1/M00/00/00/wKgZgF1tFgqAN7TQAACmPCO7q3w466.png

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

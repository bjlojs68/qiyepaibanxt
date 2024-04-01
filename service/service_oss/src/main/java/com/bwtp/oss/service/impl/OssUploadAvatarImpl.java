package com.bwtp.oss.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.bwtp.oss.service.OssUploadAvatar;
import com.bwtp.oss.utils.ConstantPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class OssUploadAvatarImpl implements OssUploadAvatar {

    @Override
    public String UploadAvatarFiles(MultipartFile file) {

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantPropertiesUtils.END_POINT;
        String accessKeyId = ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        String bucketName = ConstantPropertiesUtils.BUCKET_NAME;
        String url=null;


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            String filename = file.getOriginalFilename();

//            //yyyy/MM/dd
            String dateTime = new DateTime().toString("yyyy/MM/dd");
//
//            //生成的uuid具有-。可将其替换
            String s = UUID.randomUUID().toString().replace("-", "");
            filename=dateTime+"/"+s+filename;
            // 创建PutObject请求。
            ossClient.putObject(bucketName, filename, inputStream);
            //https://edu-blp.oss-cn-hangzhou.aliyuncs.com/1.jpg

            url="https://"+bucketName+"."+endpoint+"/"+filename;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            ossClient.shutdown();
            return url;
        }
    }
}

package com.bwtp.oss.controller;

import com.bwtp.commonutils.R;
import com.bwtp.oss.service.OssUploadAvatar;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/ossAvatarService/fileOss")
@CrossOrigin
public class OssController {

    @Resource
    private OssUploadAvatar ossUploadAvatar;
    @PostMapping("OssAvatar")
    //获取上传的文件  MultipartFile
    public R OssAvatarFile(MultipartFile file){

        //调用service里上传文件的方法，并获取该图片在oss里的路径，保存到数据库中
       String url= ossUploadAvatar.UploadAvatarFiles(file);

        return R.ok().data("url",url);
    }
}

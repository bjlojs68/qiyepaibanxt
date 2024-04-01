package com.bwtp.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssUploadAvatar {
    String UploadAvatarFiles(MultipartFile file);
}

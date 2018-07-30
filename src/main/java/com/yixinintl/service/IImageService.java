package com.yixinintl.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.oss.OSSException;


public interface IImageService {
	/*
	 * 第一个是文件参数，第二个是目录类似  "star/"这种
	 */
	public String postFileToOSS(File file,String uploadDir);
	
	public String postFileToOSS(CommonsMultipartFile file,String uploadDir,String obj) throws OSSException, ClientException, IOException;
	
	public String postFileToOSS(CommonsMultipartFile file,String uploadDir,
			String bucketName,String bucketUrl,String obj) throws OSSException, ClientException, IOException;
	
	public List<String> postFilesToOSS(List<File> files,String uploadDir);
	//用户头像png
	public String postImgToOSS(InputStream inputStream,String uploadDir);

	public void deleteFileOnOSS(String bucketName,String key) throws OSSException,ClientException;
}

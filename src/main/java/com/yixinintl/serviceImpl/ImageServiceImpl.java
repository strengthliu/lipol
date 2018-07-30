package com.heysound.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mysql.cj.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.google.common.io.Files;
import com.heysound.service.IImageService;

@Service
public class ImageServiceImpl implements IImageService {
 
	@Value("${aliyun.access_id}")
    private   String accessId;
	
	@Value("${aliyun.access_key}")
    private  String accessKey;
	
	@Value("${oss.shop.bucket}")
    private  String bucketName;

	@Value("${oss.video.image}")
	private String imageDir;//封面oss存储路径

	@Value("${oss.image.endpoint}")
	private String endpoint;
	
	private static final String topUrl="http://oss.haishengyue.com/";
	
	@Override
	public String postFileToOSS(File file, String uploadDir) {
		String ext = Files.getFileExtension(file.getName());
		String key = UUID.randomUUID().toString() + "." + ext;
		OSSClient client = new OSSClient(accessId, accessKey);
		String obj = uploadDir + key;
		try {
			uploadFile(client, bucketName, obj, file,ext);
		} catch (OSSException | ClientException | IOException e) {
			e.printStackTrace();
			return null;
		}
          return topUrl+obj;
	}
	
	@Override
	public String postFileToOSS(CommonsMultipartFile file, String uploadDir,
			String obj) throws OSSException,ClientException,IOException{
        String ext=Files.getFileExtension(file.getOriginalFilename()) == "" ? "jpg" : Files.getFileExtension(file.getOriginalFilename());
        OSSClient client = new OSSClient("http://oss-cn-shanghai.aliyuncs.com", accessId, accessKey);
        if(StringUtils.isNullOrEmpty(obj)){
            String key=UUID.randomUUID().toString()+"."+ext;//生成文件名称      
            obj=uploadDir+key;
        }
        
		uploadFile(client, bucketName,obj,file);

        return topUrl+obj;
	}
	
	@Override
	public String postFileToOSS(CommonsMultipartFile file, String uploadDir, String bucketName,
			String bucketUrl,String obj) throws OSSException,ClientException,IOException{
        String ext=Files.getFileExtension(file.getOriginalFilename());
        OSSClient client = new OSSClient(accessId, accessKey);
        
        if(StringUtils.isNullOrEmpty(obj)){
            String key=UUID.randomUUID().toString()+"."+ext;//生成文件名称
            obj=uploadDir+key;
        	
        }
	
		uploadFile(client, bucketName,obj,file);

        return bucketUrl+obj;
	}

	
    private  void uploadFile(OSSClient client, String bucketName, String key, File file,String ext)
            throws OSSException, ClientException, IOException {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.length());
        if (ext.equalsIgnoreCase("jpg")) {
        	  objectMeta.setContentType("image/jpeg");//jpg格式	
		}else if (ext.equalsIgnoreCase("png")) {
			 objectMeta.setContentType("image/png");//png格式	
		}else if(ext.equalsIgnoreCase("html")||ext.equalsIgnoreCase("htm")){
			objectMeta.setContentType("text/html");
		}
        //其他文件使用默认
        // 可以在metadata中标记文件类型
        InputStream input = new FileInputStream(file);
        client.putObject(bucketName, key, input, objectMeta);
    }
	
    
    private  void uploadFile(OSSClient client, String bucketName, String key, InputStream inputStream)
            throws OSSException, ClientException, IOException {
        ObjectMetadata objectMeta = new ObjectMetadata();
        
       // File file=  convertInputStreamToFile(inputStream);
       // InputStream innerInput = new FileInputStream(file);
        objectMeta.setContentLength(inputStream.available());//(file.length());//
        //默认上传图片
        objectMeta.setContentType("image/jpg");
       // objectMeta.setContentType("image/jpg");
        client.putObject(bucketName, key, inputStream, objectMeta);
        
    }
    
    
    public File convertInputStreamToFile(InputStream inputStream) 
      throws IOException {
        //InputStream initialStream = new FileInputStream(new File("src/main/resources/sample.txt"));
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
     
        File targetFile = new File("D://aa.jpg");
        Files.write(buffer, targetFile);
        return targetFile;
    }
    
    
    
    // 上传文件
    private  void uploadFile(OSSClient client, String bucketName, String key, MultipartFile file)
            throws OSSException, ClientException, IOException {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.getSize());
        objectMeta.setContentType(file.getContentType());//"image/jpeg"jpg格式,不然会出现下载的现象
        // 可以在metadata中标记文件类型
        client.putObject(bucketName, key, file.getInputStream(), objectMeta);
    }




	@Override
	public List<String> postFilesToOSS(List<File> files, String uploadDir) {
		List<String> urlList=new ArrayList<>();
		for(File file:files){
			urlList.add(this.postFileToOSS(file, uploadDir));
		}
		return urlList;
	}

	@Override
	public String postImgToOSS(InputStream inputStream, String uploadDir) {
		String key = UUID.randomUUID().toString() + ".jpg" ;
		OSSClient client = new OSSClient("http://" + endpoint , accessId, accessKey);
		String obj = uploadDir + key;
		try {
			uploadFile(client, bucketName, obj,inputStream);
		} catch (OSSException | ClientException | IOException e) {
			e.printStackTrace();
			return null;
		}
          return "http://" + bucketName + "." + endpoint + "/" + imageDir + key;
	}

	@Override
	public void deleteFileOnOSS(String bucketName, String key) throws OSSException,ClientException{
		OSSClient client = new OSSClient(accessId, accessKey);

		client.deleteObject(bucketName, key);
	}

}

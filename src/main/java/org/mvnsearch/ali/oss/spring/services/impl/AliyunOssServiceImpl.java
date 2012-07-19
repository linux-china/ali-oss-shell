package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.Bucket;
import com.aliyun.openservices.oss.model.OSSObject;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import org.apache.commons.io.IOUtils;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

/**
 * aliyun OSS service implementation
 *
 * @author linux_china
 */
@Component("aliyunOssService")
public class AliyunOssServiceImpl implements AliyunOssService {
    /**
     * end point
     */
    private String endpoint = "http://storage.aliyun.com";
    /**
     * access ID
     */
    private String accessId;
    /**
     * access key
     */
    private String accessKey;
    /**
     * oss client
     */
    private OSSClient oss;
    /**
     * mime types
     */
    private static ConfigurableMimeFileTypeMap mimeTypes = new ConfigurableMimeFileTypeMap();

    /**
     * construct method
     */
    public AliyunOssServiceImpl() {
        try {
            Properties properties = new Properties();
            File userHome = new File(System.getProperty("user.home"));
            File cfgFile = new File(userHome, ".aliyunoss.cfg");
            if (cfgFile.exists()) {
                properties.load(new FileInputStream(cfgFile));
                this.accessId = properties.getProperty("accessId");
                this.accessKey = properties.getProperty("accessKey");
                if (accessId != null) {
                    oss = new OSSClient(endpoint, accessId, accessKey);
                }
            }
        } catch (Exception ignore) {

        }
    }

    /**
     * available info
     *
     * @return available infor
     */
    @Override
    public boolean available() {
        return accessId != null && accessKey != null;
    }

    /**
     * set access info
     *
     * @param accessId  access id
     * @param accessKey access key
     */
    public void setAccessInfo(String accessId, String accessKey) {
        this.accessId = accessId;
        this.accessKey = accessKey;
        if (accessId != null) {
            oss = new OSSClient(endpoint, accessId, accessKey);
        }
        try {
            Properties properties = new Properties();
            properties.setProperty("accessId", accessId);
            properties.setProperty("accessKey", accessKey);
            File userHome = new File(System.getProperty("user.home"));
            File cfgFile = new File(userHome, ".aliyunoss.cfg");
            properties.store(new FileOutputStream(cfgFile), null);
        } catch (Exception ignore) {

        }
    }

    /**
     * list buckets
     *
     * @return bucket list
     * @throws Exception exception
     */
    public List<Bucket> getBuckets() throws Exception {
        return oss.listBuckets();
    }

    /**
     * list
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object metadata list
     */
    @Override
    public ObjectListing list(String bucketName, String path) throws Exception {
        if (path == null) {
            path = "";
        }
        path = path.trim();
        if (path.endsWith("*")) {
            path = path.substring(0, path.length() - 1);
        }
        return oss.listObjects(bucketName, path);
    }

    /**
     * put file to OSS
     *
     * @param bucketName     bucket name
     * @param sourceFilePath source file path
     * @param destFilePath   dest file path
     * @return oss file path
     */
    @Override
    public String put(String bucketName, String sourceFilePath, String destFilePath) throws Exception {
        byte[] content = IOUtils.toByteArray(new FileInputStream(sourceFilePath));
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mimeTypes.getContentType(sourceFilePath));
        objectMetadata.setContentLength(content.length);
        oss.putObject(bucketName, destFilePath, new ByteArrayInputStream(content), objectMetadata);
        return destFilePath;
    }

    /**
     * get file and save into local disk
     *
     * @param bucketName     bucket name
     * @param sourceFilePath source file path
     * @param destFilePath   dest file path
     * @return local file path
     */
    @Override
    public String get(String bucketName, String sourceFilePath, String destFilePath) throws Exception {
        OSSObject ossObject = oss.getObject(bucketName, sourceFilePath);
        if (ossObject != null) {
            FileOutputStream fos = new FileOutputStream(destFilePath);
            IOUtils.copy(ossObject.getObjectContent(), fos);
            fos.close();
        }
        return destFilePath;
    }

    /**
     * get oss object
     *
     * @param bucketName bucket name
     * @param filePath   file path
     * @return oss object
     */
    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String filePath) throws Exception {
        return oss.getObjectMetadata(bucketName, filePath);
    }

    /**
     * set object meta data
     *
     * @param bucketName bucket name
     * @param filePath   file path
     * @param key        key
     * @param value      value
     */
    public void setObjectMetadata(String bucketName, String filePath, String key, String value) throws Exception {
        OSSObject ossObject = oss.getObject(bucketName, filePath);
        ObjectMetadata objectMetadata = ossObject.getObjectMetadata();
        objectMetadata.getRawMetadata().put(key, value);
        oss.putObject(bucketName, filePath, ossObject.getObjectContent(), objectMetadata);
    }
}

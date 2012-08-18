package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.*;
import org.apache.commons.io.IOUtils;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
     * config service
     */
    private ConfigService configService;
    /**
     * oss client
     */
    private OSSClient oss;

    /**
     * mime types
     */
    private static ConfigurableMimeFileTypeMap mimeTypes = new ConfigurableMimeFileTypeMap();

    /**
     * inject config service
     *
     * @param configService config service
     */
    @Autowired
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * refresh token
     */
    @PostConstruct
    public void refreshToken() {
        String accessId = configService.getProperty("ACCESS_ID");
        String accessKey = configService.getProperty("ACCESS_KEY");
        if (accessId != null) {
            oss = new OSSClient(endpoint, accessId, accessKey);
        }
    }

    /**
     * get oss client
     *
     * @return oss client
     */
    public OSSClient getOssClient() {
        return oss;
    }

    /**
     * create bucket
     *
     * @param bucket bucket name
     * @throws Exception exception
     */
    public void createBucket(String bucket) throws Exception {
        oss.createBucket(bucket);
    }

    /**
     * delete bucket
     *
     * @param bucket bucket
     * @throws Exception exception
     */
    public void deleteBucket(String bucket) throws Exception {
        oss.deleteBucket(bucket);
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
     * get bucket ACL
     *
     * @param bucket bucket name
     * @return ACL String, such --, RW, R-
     * @throws Exception exception
     */
    public String getBucketACL(String bucket) throws Exception {
        String aclStr = "--";
        AccessControlList acl = oss.getBucketAcl(bucket);
        for (Grant grant : acl.getGrants()) {
            if (grant.getGrantee() == GroupGrantee.AllUsers) {
                if (grant.getPermission() == Permission.Read) {
                    aclStr = "R-";
                }
                if (grant.getPermission() == Permission.FullControl) {
                    aclStr = "RW";
                }
            }
        }
        return aclStr;
    }

    /**
     * 设置ACL
     *
     * @param bucket bucket
     * @param acl    acl value, such --, R- or RW
     * @throws Exception exception
     */
    @Override
    public void setBucketACL(String bucket, String acl) throws Exception {
        if (acl.equals("RW")) {
            oss.setBucketAcl(bucket, CannedAccessControlList.PublicReadWrite);
        } else if (acl.equals("R-")) {
            oss.setBucketAcl(bucket, CannedAccessControlList.PublicRead);
        } else {
            oss.setBucketAcl(bucket, CannedAccessControlList.Private);
        }
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
     * copy object
     *
     * @param sourceBucketName source bucket name
     * @param sourceFilePath   source file path
     * @param destBucketName   dest bucket name
     * @param destFilePath     dest file path
     * @return new file path
     * @throws Exception exception
     */
    public String copy(String sourceBucketName, String sourceFilePath, String destBucketName, String destFilePath) throws Exception {
        oss.copyObject(sourceBucketName, sourceFilePath, destBucketName, destFilePath);
        return new OSSUri(destBucketName, destFilePath).toString();
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
     * delete object
     *
     * @param bucketName     bucket name
     * @param sourceFilePath source file path
     * @throws Exception exception
     */
    @Override
    public void delete(String bucketName, String sourceFilePath) throws Exception {
        oss.deleteObject(bucketName, sourceFilePath);
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

package org.mvnsearch.ali.oss.spring.services;

import com.aliyun.openservices.oss.model.Bucket;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * aliyun OSS service
 *
 * @author linux_china
 */
public interface AliyunOssService {
    /**
     * available info
     *
     * @return available infor
     */
    public boolean available();

    /**
     * set access info
     *
     * @param accessId  access id
     * @param accessKey access key
     */
    public void setAccessInfo(String accessId, String accessKey);

    /**
     * create bucket
     *
     * @param bucket bucket name
     * @throws Exception exception
     */
    public void createBucket(String bucket) throws Exception;

    /**
     * delete bucket
     *
     * @param bucket bucket
     * @throws Exception exception
     */
    public void deleteBucket(String bucket) throws Exception;

    /**
     * list buckets
     *
     * @return bucket list
     * @throws Exception exception
     */
    public List<Bucket> getBuckets() throws Exception;

    /**
     * get bucket ACL
     *
     * @param bucket bucket name
     * @return ACL String, such --, RW, R-
     * @throws Exception exception
     */
    public String getBucketACL(String bucket) throws Exception;

    /**
     * 设置ACL
     *
     * @param bucket bucket
     * @param acl    acl value, such --, R- or RW
     * @throws Exception exception
     */
    public void setBucketACL(String bucket, String acl) throws Exception;

    /**
     * list
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object metadata list
     */
    public ObjectListing list(String bucketName, String path) throws Exception;

    /**
     * put file to OSS
     *
     * @param bucketName     bucket name
     * @param sourceFilePath source file path
     * @param destFilePath   dest file path
     * @return oss file path
     */
    public String put(String bucketName, String sourceFilePath, String destFilePath) throws Exception;

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
    public String copy(String sourceBucketName, String sourceFilePath, String destBucketName, String destFilePath) throws Exception;

    /**
     * get file and save into local disk
     *
     * @param bucketName     bucket name
     * @param sourceFilePath source file path
     * @param destFilePath   dest file path
     * @return local file path
     */
    public String get(String bucketName, String sourceFilePath, String destFilePath) throws Exception;

    /**
     * delete object
     *
     * @param bucketName     bucket name
     * @param sourceFilePath source file path
     * @throws Exception exception
     */
    public void delete(String bucketName, String sourceFilePath) throws Exception;

    /**
     * get oss object
     *
     * @param bucketName bucket name
     * @param filePath   file path
     * @return oss object
     */
    public ObjectMetadata getObjectMetadata(String bucketName, String filePath) throws Exception;

    /**
     * set object meta data
     *
     * @param bucketName bucket name
     * @param filePath   file path
     * @param key        key
     * @param value      value
     */
    public void setObjectMetadata(String bucketName, String filePath, String key, String value) throws Exception;
}

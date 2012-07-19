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
     * list buckets
     *
     * @return bucket list
     * @throws Exception exception
     */
    public List<Bucket> getBuckets() throws Exception;

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

package org.mvnsearch.ali.oss.spring.services;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * aliyun OSS service
 *
 * @author linux_china
 */
public interface AliyunOssService {
    /**
     * max object count for listing
     */
    Integer MAX_OBJECTS = 500;

    /**
     * refresh token
     */
    void refreshToken();

    /**
     * get OSS client
     *
     * @return oss client
     */
    OSSClient getOssClient();

    /**
     * create bucket
     *
     * @param bucket bucket name
     * @throws Exception exception
     */
    void createBucket(String bucket) throws Exception;

    /**
     * drop bucket
     *
     * @param bucket bucket
     * @throws Exception exception
     */
    void dropBucket(String bucket) throws Exception;

    /**
     * delete bucket
     *
     * @param bucket bucket
     * @throws Exception exception
     */
    void deleteBucket(String bucket) throws Exception;

    /**
     * list buckets
     *
     * @return bucket list
     * @throws Exception exception
     */
    List<Bucket> getBuckets() throws Exception;

    /**
     * get bucket by name
     *
     * @param name name
     * @return bucket object
     * @throws Exception exception
     */
    @Nullable
    Bucket getBucket(String name) throws Exception;

    /**
     * get bucket ACL
     *
     * @param bucket bucket name
     * @return ACL String, such --, RW, R-
     * @throws Exception exception
     */
    String getBucketACL(String bucket) throws Exception;

    /**
     * 设置ACL
     *
     * @param bucket bucket
     * @param acl    acl value, such --, R- or RW
     * @throws Exception exception
     */
    void setBucketACL(String bucket, String acl) throws Exception;

    /**
     * list children recursively
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object metadata list
     */
    ObjectListing list(String bucketName, String path) throws Exception;

    /**
     * list children recurly
     *
     * @param bucketName bucket name
     * @param path       path
     * @param maxResults max results
     * @return object listing
     * @throws Exception exception
     */
    ObjectListing list(String bucketName, String path, int maxResults) throws Exception;

    /**
     * list children only
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object listing
     * @throws Exception exception
     */
    ObjectListing listChildren(String bucketName, String path) throws Exception;

    /**
     * list children only
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object listing
     * @throws Exception exception
     */
    ObjectListing listChildren(String bucketName, String path, int maxResults) throws Exception;

    /**
     * put local file to OSS
     *
     * @param sourceFilePath source file path on local disk
     * @param destObject     dest object
     * @return oss file path
     */
    ObjectMetadata put(String sourceFilePath, OSSUri destObject) throws Exception;

    /**
     * put local file to OSS with zip
     *
     * @param sourceFilePath source file path on local disk
     * @param destObject     dest object
     * @return oss file path
     */
    ObjectMetadata put(String sourceFilePath, OSSUri destObject, Boolean zip) throws Exception;

    /**
     * copy object
     *
     * @param sourceObjectUri source object uri
     * @param destObjectUri   dest object uri
     * @return new file path
     * @throws Exception exception
     */
    String copy(OSSUri sourceObjectUri, OSSUri destObjectUri) throws Exception;

    /**
     * get file and save into local disk
     *
     * @param objectUri    object uri
     * @param destFilePath dest file path
     * @return local file path
     */
    String get(OSSUri objectUri, String destFilePath) throws Exception;

    /**
     * delete object
     *
     * @param objectUri bucket uri
     * @throws Exception exception
     */
    void delete(OSSUri objectUri) throws Exception;

    /**
     * get oss object
     *
     * @param objectUri object uri
     * @return oss object
     */
    @Nullable
    ObjectMetadata getObjectMetadata(OSSUri objectUri) throws Exception;

    /**
     * get OSS object
     *
     * @param objectUri object uri
     * @return OSS object
     * @throws Exception exception
     */
    @Nullable
    OSSObject getOssObject(OSSUri objectUri) throws Exception;

    /**
     * set object meta data
     *
     * @param objectUri object uri
     * @param key       key
     * @param value     value
     */
    void setObjectMetadata(OSSUri objectUri, String key, String value) throws Exception;
}

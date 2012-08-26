package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.cookie.DateUtils;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * aliyun OSS service implementation
 *
 * @author linux_china
 */
@Component("aliyunOssService")
public class AliyunOssServiceImpl implements AliyunOssService {
    /**
     * log
     */
    private Logger log = LoggerFactory.getLogger(AliyunOssServiceImpl.class);
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
     * drop bucket
     *
     * @param bucket bucket
     * @throws Exception exception
     */
    public void dropBucket(String bucket) throws Exception {
        oss.deleteBucket(bucket);
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
     * get bucket by name
     *
     * @param name name
     * @return bucket object
     * @throws Exception exception
     */
    @Nullable
    public Bucket getBucket(String name) throws Exception {
        List<Bucket> buckets = oss.listBuckets();
        for (Bucket bucket : buckets) {
            if (bucket.getName().equals(name)) {
                return bucket;
            }
        }
        return null;
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
        } else if (acl.equals("R-") || acl.equals("R")) {
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
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        request.setPrefix(path);
        request.setMaxKeys(MAX_OBJECTS);
        return oss.listObjects(request);
    }

    /**
     * list children only
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object listing
     * @throws Exception exception
     */
    public ObjectListing listChildren(String bucketName, String path) throws Exception {
        if (path == null || path.equals("/")) {
            path = "";
        }
        if (!path.isEmpty() && !path.endsWith("/")) {
            path = path + "/";
        }
        path = path.trim();
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        request.setPrefix(path);
        request.setDelimiter("/");
        request.setMaxKeys(MAX_OBJECTS);
        return oss.listObjects(request);
    }

    /**
     * put local file to OSS
     *
     * @param sourceFilePath source file path on local disk
     * @param destObject     dest object
     * @return oss file path
     */
    public ObjectMetadata put(String sourceFilePath, OSSUri destObject) throws Exception {
        byte[] content = IOUtils.toByteArray(new FileInputStream(sourceFilePath));
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mimeTypes.getContentType(sourceFilePath));
        objectMetadata.setContentLength(content.length);
        oss.putObject(destObject.getBucket(), destObject.getFilePath(), new ByteArrayInputStream(content), objectMetadata);
        return objectMetadata;
    }

    /**
     * copy object
     *
     * @param sourceObjectUri source object uri
     * @param destObjectUri   dest object uri
     * @return new file path
     * @throws Exception exception
     */
    public String copy(OSSUri sourceObjectUri, OSSUri destObjectUri) throws Exception {
        oss.copyObject(sourceObjectUri.getBucket(), sourceObjectUri.getFilePath(), destObjectUri.getBucket(), destObjectUri.getFilePath());
        return destObjectUri.toString();
    }


    /**
     * get file and save into local disk
     *
     * @param objectUri    object uri
     * @param destFilePath dest file path
     * @return local file path
     */
    public String get(OSSUri objectUri, String destFilePath) throws Exception {
        OSSObject ossObject = oss.getObject(objectUri.getBucket(), objectUri.getFilePath());
        File destFile = new File(destFilePath);
        if (ossObject != null) {
            if (destFile.isDirectory()) {
                destFile = new File(destFile, objectUri.getFileName());
            }
            if (!destFile.getParentFile().exists()) {
                FileUtils.forceMkdir(destFile.getParentFile());
            }
            FileOutputStream fos = new FileOutputStream(destFilePath);
            IOUtils.copy(ossObject.getObjectContent(), fos);
            fos.close();
        }
        return destFile.getAbsolutePath();
    }

    /**
     * delete object
     *
     * @param objectUri bucket uri
     * @throws Exception exception
     */
    public void delete(OSSUri objectUri) throws Exception {
        oss.deleteObject(objectUri.getBucket(), objectUri.getFilePath());
    }

    /**
     * get oss object
     *
     * @param objectUri object uri
     * @return oss object
     */
    @Nullable
    public ObjectMetadata getObjectMetadata(OSSUri objectUri) throws Exception {
        try {
            return oss.getObjectMetadata(objectUri.getBucket(), objectUri.getFilePath());
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * get OSS object
     *
     * @param objectUri object uri
     * @return OSS object
     * @throws Exception exception
     */
    @Nullable
    public OSSObject getOssObject(OSSUri objectUri) throws Exception {
        return oss.getObject(objectUri.getBucket(), objectUri.getFilePath());
    }

    /**
     * set object meta data
     *
     * @param objectUri object uri
     * @param key       key
     * @param value     value
     */
    public void setObjectMetadata(OSSUri objectUri, String key, String value) throws Exception {
        ObjectMetadata objectMetadata = oss.getObjectMetadata(objectUri.getBucket(), objectUri.getFilePath());
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(objectUri.getBucket(), objectUri.getFilePath(),
                objectUri.getBucket(), objectUri.getFilePath());
        if (key.equalsIgnoreCase("Cache-Control")) {
            objectMetadata.setCacheControl(value);
        } else if (key.equals("Content-Type")) {
            objectMetadata.setContentType(value);
        } else if (key.equalsIgnoreCase("Expires")) {
            objectMetadata.setExpirationTime(DateUtils.parseDate(value, new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"}));
        } else {
            if (objectMetadata.getUserMetadata() == null || objectMetadata.getUserMetadata().isEmpty()) {
                objectMetadata.setUserMetadata(new HashMap<String, String>());
            }
            objectMetadata.getUserMetadata().put(key, value);
        }
        copyObjectRequest.setNewObjectMetadata(objectMetadata);
        oss.copyObject(copyObjectRequest);
    }
}

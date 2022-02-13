package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.mvnsearch.ali.oss.spring.services.ZipUtils;
import org.mvnsearch.ali.oss.spring.shell.converters.BucketEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * aliyun OSS service implementation
 *
 * @author linux_china
 */
@SuppressWarnings("RedundantThrows")
@Component("aliyunOssService")
public class AliyunOssServiceImpl implements AliyunOssService {
    /**
     * end point
     */
    private static final String DEFAULT_ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
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
    private static final ConfigurableMimeFileTypeMap mimeTypes = new ConfigurableMimeFileTypeMap();

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
        String endpoint = configService.getProperty("ENDPOINT");
        if (endpoint == null) {
            endpoint = DEFAULT_ENDPOINT;
        }
        if (accessId != null) {
            oss = new OSSClient(endpoint, new DefaultCredentialProvider(accessId, accessKey), null);
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
        List<Bucket> buckets = oss.listBuckets();
        BucketEnum.reset(buckets);
        return buckets;
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
        //noinspection deprecation
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
        return list(bucketName, path, MAX_OBJECTS);
    }

    /**
     * list children recurly
     *
     * @param bucketName bucket name
     * @param path       path
     * @param maxResults max results
     * @return object listing
     * @throws Exception exception
     */
    public ObjectListing list(String bucketName, String path, int maxResults) throws Exception {
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
        request.setMaxKeys(maxResults);
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
        return listChildren(bucketName, path, MAX_OBJECTS);
    }

    /**
     * list children only
     *
     * @param bucketName bucket name
     * @param path       path
     * @return object listing
     * @throws Exception exception
     */
    public ObjectListing listChildren(String bucketName, String path, int maxResults) throws Exception {
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
        request.setMaxKeys(maxResults);
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
     * put local file to OSS with zip
     *
     * @param sourceFilePath source file path on local disk
     * @param destObject     dest object
     * @return oss file path
     */
    public ObjectMetadata put(String sourceFilePath, OSSUri destObject, Boolean zip) throws Exception {
        if (zip == null || !zip) {
            return put(sourceFilePath, destObject);
        }
        byte[] zipContent = ZipUtils.compress(IOUtils.toByteArray(new FileInputStream(sourceFilePath)));
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mimeTypes.getContentType(sourceFilePath));
        objectMetadata.setContentEncoding("gzip");
        objectMetadata.setContentLength(zipContent.length);
        oss.putObject(destObject.getBucket(), destObject.getFilePath(), new ByteArrayInputStream(zipContent), objectMetadata);
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
            if (destFile.isDirectory() && objectUri.getFileName() != null) {
                destFile = new File(destFile, objectUri.getFileName());
            }
            if (!destFile.getParentFile().exists()) {
                FileUtils.forceMkdir(destFile.getParentFile());
            }
            FileOutputStream fos = new FileOutputStream(destFilePath);
            InputStream content = ossObject.getObjectContent();
            //处理解压缩
            if ("gzip".equalsIgnoreCase(ossObject.getObjectMetadata().getContentEncoding())) {
                IOUtils.copy(new GZIPInputStream(content), fos);
            } else {
                IOUtils.copy(content, fos);
            }
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
            objectMetadata.setExpirationTime(DateUtils.parseDate(value, "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"));
        } else if (key.equalsIgnoreCase("Content-Encoding")) {
            objectMetadata.setContentEncoding(value);
        } else if (key.equalsIgnoreCase("Content-Disposition")) {
            objectMetadata.setContentDisposition(value);
        } else {
            if (objectMetadata.getUserMetadata() == null || objectMetadata.getUserMetadata().isEmpty()) {
                objectMetadata.setUserMetadata(new HashMap<>());
            }
            objectMetadata.getUserMetadata().put(key, value);
        }
        copyObjectRequest.setNewObjectMetadata(objectMetadata);
        oss.copyObject(copyObjectRequest);
    }
}

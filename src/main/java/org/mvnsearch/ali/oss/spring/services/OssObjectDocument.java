package org.mvnsearch.ali.oss.spring.services;

import com.aliyun.openservices.oss.model.OSSObjectSummary;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

import java.util.Date;

/**
 * oss object document
 *
 * @author linux_china
 */
public class OssObjectDocument {
    private static ConfigurableMimeFileTypeMap mimeTypes = new ConfigurableMimeFileTypeMap();
    /**
     * bucket
     */
    private String bucket;
    /**
     * path
     */
    private String path;
    /**
     * name
     */
    private String name;
    /**
     * created timestamp
     */
    private Date date;
    /**
     * content type
     */
    private String contentType;
    /**
     * content length
     */
    private Integer contentLength;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? "" : path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * get object uri
     *
     * @return object uri
     */
    public String getObjectUri() {
        String objectKey = StringUtils.defaultIfEmpty(path, "") + "/" + name;
        return new OSSUri(bucket, objectKey).toString();
    }

    /**
     * construct from object summary
     *
     * @param objectSummary object summary
     * @return object document
     */
    public static OssObjectDocument constructFromObjectSummary(OSSObjectSummary objectSummary) {
        OssObjectDocument document = new OssObjectDocument();
        document.setBucket(objectSummary.getBucketName());
        String objectKey = objectSummary.getKey();
        if (objectKey.contains("/")) {
            document.setPath(objectKey.substring(0, objectKey.lastIndexOf("/")));
            document.setName(objectKey.substring(objectKey.lastIndexOf("/") + 1));
        } else {
            document.setName(objectKey);
        }
        document.setContentType(mimeTypes.getContentType(document.getName()));
        document.setContentLength((int) objectSummary.getSize());
        document.setDate(objectSummary.getLastModified());
        return document;
    }
}

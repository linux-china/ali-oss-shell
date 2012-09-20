package org.mvnsearch.ali.oss.spring.services;

import java.util.Date;

/**
 * oss object document
 *
 * @author linux_china
 */
public class OssObjectDocument {
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
        this.path = path;
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
}

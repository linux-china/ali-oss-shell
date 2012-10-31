package org.mvnsearch.ali.oss.spring.services;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * OSS URI, format as oss://bucket/path/file.txt
 *
 * @author linux_china
 */
public class OSSUri {
    /**
     * oss protocol
     */
    public static String PROTOCOL = "oss://";
    /**
     * bucket name
     */
    private String bucket;
    /**
     * file path
     */
    private String filePath;

    /**
     * construct method
     */
    public OSSUri() {

    }

    /**
     * construct method
     *
     * @param bucket   bucket name
     * @param filePath file path
     */
    public OSSUri(String bucket, String filePath) {
        setBucket(bucket);
        setFilePath(filePath);
    }

    /**
     * construct method
     *
     * @param uri oss uri，目前支持oss://和http://两种方式
     */
    public OSSUri(String uri) {
        String relativePath = uri;
        if (uri.startsWith(PROTOCOL)) {
            relativePath = uri.replace(PROTOCOL, "");
        } else if (uri.startsWith("http://storage.aliyun.com/")) {
            relativePath = uri.replace("http://storage.aliyun.com/", "");
        }
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        String[] parts = relativePath.split("/", 2);
        if (parts.length > 0) {
            this.bucket = parts[0];
        }
        if (parts.length > 1) {
            this.filePath = parts[1];
        }
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(@NotNull String bucket) {
        this.bucket = bucket;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(@Nullable String filePath) {
        this.filePath = filePath == null ? "" : filePath;
        if (this.filePath.startsWith("/")) {
            this.filePath = this.filePath.substring(1, this.filePath.length());
        }
    }

    /**
     * get file path
     *
     * @return file path, if absent, empty string will be returned
     */
    @NotNull
    public String getPath() {
        if (filePath == null || filePath.isEmpty() || !filePath.contains("/")) {
            return "";
        } else {
            return filePath.substring(0, filePath.lastIndexOf("/"));
        }
    }

    /**
     * get file name
     *
     * @return file name
     */
    @Nullable
    public String getFileName() {
        if (isDirectory()) {
            return null;
        }
        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        } else {
            return filePath;
        }
    }

    /**
     * 判断是否为目录
     *
     * @return 目录标识
     */
    public boolean isDirectory() {
        return filePath == null || filePath.isEmpty() || filePath.endsWith("/");
    }

    /**
     * 是否是合法的URI
     *
     * @return 合法标识
     */
    public boolean isValid() {
        return bucket != null;
    }

    /**
     * oss uri
     *
     * @return oss uri
     */
    public String toString() {
        if (filePath != null && !filePath.isEmpty()) {
            return PROTOCOL + bucket + "/" + filePath;
        } else {
            return PROTOCOL + bucket;
        }
    }

    /**
     * 获取HTTP URL
     *
     * @return http url
     */
    public String getHttpUrl() {
        return "http://" + bucket + ".oss.aliyuncs.com/" + StringUtils.defaultIfEmpty(filePath, "");
    }

    /**
     * get child object url, if file path starts with os://, new OssUri will be created
     *
     * @param childFilePath child file path
     * @return object oss uri
     */
    public OSSUri getChildObjectUri(String childFilePath) {
        if (childFilePath != null && childFilePath.startsWith("oss://")) {
            return new OSSUri(childFilePath);
        }
        //考虑使用相对路径
        String tempFilePath = childFilePath;
        if (this.filePath != null) {
            if (childFilePath == null || !childFilePath.startsWith(filePath)) {
                tempFilePath = filePath + StringUtils.defaultIfEmpty(childFilePath, "");
            }
        }
        //处理相对目录
        if (tempFilePath != null && tempFilePath.endsWith("../")) {
            tempFilePath = tempFilePath.replace("../", "");
            if (tempFilePath.endsWith("/")) {
                tempFilePath = tempFilePath.substring(0, tempFilePath.length() - 1);
            }
            if (tempFilePath.contains("/")) {
                tempFilePath = tempFilePath.substring(0, tempFilePath.lastIndexOf("/")) + "/";
            } else {
                tempFilePath = "";
            }
        }
        return new OSSUri(this.bucket, tempFilePath);
    }

    /**
     * get path in repository
     *
     * @param repository repository directory
     * @return absolute path
     */
    public File getPathInRepository(File repository) {
        return new File(repository, bucket + "/" + filePath);
    }
}

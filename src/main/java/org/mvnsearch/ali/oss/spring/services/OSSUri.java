package org.mvnsearch.ali.oss.spring.services;

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
     * @param uri oss uri
     */
    public OSSUri(String uri) {
        if (uri.startsWith("oss://")) {
            String temp = uri.replace(PROTOCOL, "");
            String[] parts = temp.split("/", 2);
            if (parts.length > 0) {
                this.bucket = parts[0];
            }
            if (parts.length > 1) {
                this.filePath = parts[1];
            }
        }
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        if (filePath != null && filePath.startsWith("/")) {
            this.filePath = filePath.substring(1, filePath.length());
        }
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
        return toString().replaceAll("oss://", "http://storage.aliyun.com/");
    }
}

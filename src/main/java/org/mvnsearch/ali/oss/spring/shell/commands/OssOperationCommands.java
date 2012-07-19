package org.mvnsearch.ali.oss.spring.shell.commands;

import com.aliyun.openservices.oss.model.Bucket;
import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import org.apache.http.impl.cookie.DateUtils;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.util.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aliyun OSS operation commands
 *
 * @author linux_china
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
@Component
public class OssOperationCommands implements CommandMarker {
    /**
     * current bucket
     */
    private String currentBucket = null;
    /**
     * current directory
     */
    private String currentDir = null;
    /**
     * aliyun oss service
     */
    private AliyunOssService aliyunOssService;

    /**
     * inject aliyun oss service
     *
     * @param aliyunOssService aliyun oss service
     */
    @Autowired
    public void setAliyunOssService(AliyunOssService aliyunOssService) {
        this.aliyunOssService = aliyunOssService;
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "config", help = "Config the Aliyun OSS access info")
    public String config(@CliOption(key = {"id"}, mandatory = true, help = "Aliyun Access ID") final String accessId,
                         @CliOption(key = {"key"}, mandatory = true, help = "Aliyun Access Key") final String accessKey) {
        aliyunOssService.setAccessInfo(accessId, accessKey);
        return "Access info saved!";
    }

    /**
     * list buckets
     *
     * @return bucket list
     */
    @CliCommand(value = "df", help = "Display buckets")
    public String df() {
        return listBuckets();
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "get", help = "Get the file from OSS and save it to local disk")
    public String get(@CliOption(key = {"source"}, mandatory = true, help = "source file path on OSS") final String sourceFilePath,
                      @CliOption(key = {"dest"}, mandatory = true, help = "destination file path on disk") final String destFilePath) {
        if (currentBucket == null) {
            return "Please select a bucket!";
        }
        File destFile = new File(destFilePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        try {
            return "Saved to " + aliyunOssService.get(currentBucket, sourceFilePath, destFilePath);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "put", help = "Put the local file to OSS")
    public String put(@CliOption(key = {"source"}, mandatory = true, help = "source file path on disk") final String sourceFilePath,
                      @CliOption(key = {"dest"}, mandatory = true, help = "destination file path on OSS") final String destFilePath) {
        if (currentBucket == null) {
            return "Please select a bucket!";
        }
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            return "File not extis: " + sourceFilePath;
        }
        try {
            aliyunOssService.put(currentBucket, sourceFilePath, destFilePath);
            return "Uploaded to: oss://" + currentBucket + "/" + destFilePath;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "ls", help = "List buckts, directories or files")
    public String ls(@CliOption(key = {""}, mandatory = false, help = "prefix wild matched file name") final String filename) {
        if (currentBucket == null) {
            return listBuckets();
        }
        StringBuilder buf = new StringBuilder();
        String prefix = StringUtils.defaultIfEmpty(currentDir, "") + StringUtils.defaultIfEmpty(filename, "");
        try {
            ObjectListing objectListing = aliyunOssService.list(currentBucket, prefix);
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                buf.append(DateUtils.formatDate(objectSummary.getLastModified(), "yyyy-MM-dd HH:mm:ss") +
                        StringUtils.padLeft(String.valueOf(objectSummary.getSize()), 10, ' ') + " " +
                        objectSummary.getKey() + StringUtils.LINE_SEPARATOR);
            }
            buf.append(objectListing.getObjectSummaries().size() + " files found");
        } catch (Exception e) {
            buf.append(e.getMessage());
        }
        return buf.toString().trim();
    }

    /**
     * list buckets
     *
     * @return bucket list
     */
    private String listBuckets() {
        StringBuilder buf = new StringBuilder();
        try {
            buf.append("Buckets:" + StringUtils.LINE_SEPARATOR);
            List<Bucket> buckets = aliyunOssService.getBuckets();
            for (Bucket bucket : buckets) {
                String pad = "    ";
                if (bucket.getName().equals(currentBucket)) {
                    pad = " => ";
                }
                buf.append(pad + "oss://" + bucket.getName() + StringUtils.LINE_SEPARATOR);
            }
        } catch (Exception e) {
            buf.append(e.getMessage());
        }
        return buf.toString().trim();
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "cd", help = "Change directory")
    public String cd(@CliOption(key = {""}, mandatory = true, help = "Change directory") final String dir) {
        if (dir == null || dir.equals("/")) {
            currentDir = "";
        } else {
            currentDir = dir + "/";
        }
        return null;
    }

    /**
     * switch bucket
     *
     * @return content
     */
    @CliCommand(value = "use", help = "Switch bucket")
    public String use(@CliOption(key = {""}, mandatory = true, help = "bucket name") final String bucketName) {
        this.currentBucket = bucketName;
        return "Switched to " + bucketName;
    }

    /**
     * switch bucket
     *
     * @return content
     */
    @CliCommand(value = "file", help = "Show OSS object detail information")
    public String file(@CliOption(key = {""}, mandatory = true, help = "file path") final String filePath) {
        StringBuilder buf = new StringBuilder();
        try {
            ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(currentBucket, filePath);
            Map<String, Object> rawMetadata = objectMetadata.getRawMetadata();
            for (Map.Entry<String, Object> entry : rawMetadata.entrySet()) {
                buf.append(StringUtils.padRight(entry.getKey(), 20, ' ') + " : " + entry.getValue() + StringUtils.LINE_SEPARATOR);
            }
            Map<String, String> userMetadata = objectMetadata.getUserMetadata();
            for (Map.Entry<String, String> entry : userMetadata.entrySet()) {
                buf.append(StringUtils.padRight(entry.getKey(), 20, ' ') + " : " + entry.getValue() + StringUtils.LINE_SEPARATOR);
            }
        } catch (Exception e) {
            buf.append(e.getMessage() + StringUtils.LINE_SEPARATOR);
        }
        return buf.toString().trim();
    }

    /**
     * switch bucket
     *
     * @return content
     */
    @CliCommand(value = "set", help = "Set object metadata")
    public String set(@CliOption(key = {"file"}, mandatory = true, help = "OSS file path") final String filePath,
                      @CliOption(key = {"key"}, mandatory = true, help = "Metadata key") final String key,
                      @CliOption(key = {"value"}, mandatory = true, help = "Metadata value") final String value) {
        try {
            aliyunOssService.setObjectMetadata(currentBucket, filePath, key, value);
        } catch (Exception e) {
            return e.getMessage();
        }
        return file(filePath);
    }
}
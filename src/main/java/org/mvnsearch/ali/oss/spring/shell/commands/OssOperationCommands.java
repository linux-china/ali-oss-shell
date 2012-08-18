package org.mvnsearch.ali.oss.spring.shell.commands;

import com.aliyun.openservices.oss.model.Bucket;
import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.http.impl.cookie.DateUtils;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collection;
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
     * config service
     */
    private ConfigService configService;
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
     * inject config service
     *
     * @param configService config service
     */
    @Autowired
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * init method: load current bucket
     */
    @PostConstruct
    public void init() {
        this.currentBucket = configService.getProperty("BUCKET");
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "config", help = "Config the Aliyun OSS access info")
    public String config(@CliOption(key = {"id"}, mandatory = true, help = "Aliyun Access ID") final String accessId,
                         @CliOption(key = {"key"}, mandatory = true, help = "Aliyun Access Key") final String accessKey) {
        configService.setAccessInfo(accessId, accessKey);
        aliyunOssService.refreshToken();
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
     * create new bucket
     *
     * @return new bucket
     */
    @CliCommand(value = "create", help = "Create a new bucket")
    public String create(@CliOption(key = {""}, mandatory = false, help = "prefix wild matched file name") final String bucket) {
        try {
            aliyunOssService.createBucket(bucket);
            this.currentBucket = bucket;
            return "Bucket 'oss://+" + bucket + "' created and switched";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * create new bucket
     *
     * @return new bucket
     */
    @CliCommand(value = "chmod", help = "Set Current Bucket Access Control List: Private, R- or RW")
    public String chmod(@CliOption(key = {""}, mandatory = false, help = "Set bucket Access Control List") String acl) {
        try {
            if (currentBucket == null) {
                return "Please select a bucket!";
            }
            if (acl != null && acl.equalsIgnoreCase("private")) {
                acl = "--";
            }
            if (acl != null && (acl.equalsIgnoreCase("--") || acl.equals("R-") || acl.equals("RW"))) {
                aliyunOssService.setBucketACL(currentBucket, acl);
            } else {
                return "ACL value should be 'Private', 'R-' or 'RW'";
            }

        } catch (Exception e) {
            return e.getMessage();
        }
        return currentBucket + "'s ACL: " + acl;
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
            if (sourceFile.isDirectory()) {
                uploadDirectory(currentBucket, destFilePath, sourceFile, false);
            } else {
                aliyunOssService.put(currentBucket, sourceFilePath, destFilePath);
                return "Uploaded to: oss://" + currentBucket + "/" + destFilePath;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    /**
     * upload directory
     *
     * @param bucket       bucket
     * @param destFilePath dest file name
     * @param sourceDir    source directory
     * @param synced       synced mark
     * @throws Exception exception
     */
    private void uploadDirectory(String bucket, String destFilePath, File sourceDir, boolean synced) throws Exception {
        Collection<File> files = FileUtils.listFiles(sourceDir, new AbstractFileFilter() {
                    public boolean accept(File file) {
                        return !file.getName().startsWith(".");
                    }
                }, new AbstractFileFilter() {
                    public boolean accept(File dir, String name) {
                        return !name.startsWith(".");
                    }
                }
        );
        for (File file : files) {
            String destPath = file.getAbsolutePath().replace(sourceDir.getAbsolutePath(), "");
            destPath = destFilePath + destPath.replaceAll("\\\\", "/");
            if (destPath.contains("//")) {
                destPath = destPath.replace("//", "/");
            }
            boolean overwrite = true;
            //sync validation
            if (synced) {
                ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(bucket, destFilePath);
                if (objectMetadata != null) {
                    if (objectMetadata.getLastModified().getTime() >= file.lastModified() && file.length() == objectMetadata.getContentLength()) {
                        overwrite = false;
                    }
                }
            }
            if (overwrite) {
                aliyunOssService.put(bucket, file.getAbsolutePath(), destPath);
                System.out.println("Uploaded: oss://" + bucket + "/" + destFilePath);
            }
        }
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "sync", help = "Put the local file to OSS")
    public String sync(@CliOption(key = {"source"}, mandatory = true, help = "source directory on disk") final String sourceDirectory,
                       @CliOption(key = {"dest"}, mandatory = true, help = "destination path on OSS") final String destPath) {
        if (currentBucket == null) {
            return "Please select a bucket!";
        }
        File sourceFile = new File(sourceDirectory);
        if (!sourceFile.exists()) {
            return "File not extis: " + sourceDirectory;
        }
        try {
            if (sourceFile.isDirectory()) {
                uploadDirectory(currentBucket, destPath, sourceFile, true);
            } else {
                aliyunOssService.put(currentBucket, sourceDirectory, destPath);
                return "Uploaded to: oss://" + currentBucket + "/" + destPath;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
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
                //acl
                buf.append(aliyunOssService.getBucketACL(bucket.getName()));
                //create time
                buf.append("  " + DateUtils.formatDate(bucket.getCreationDate(), "yyyy-MM-dd HH:mm:ss"));
                //pad
                buf.append((bucket.getName().equals(currentBucket)) ? " => " : "    ");
                //apend url
                buf.append("oss://" + bucket.getName() + StringUtils.LINE_SEPARATOR);
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
        configService.setProperty("BUCKET", bucketName);
        return "Switched to " + bucketName;
    }

    /**
     * show OSS object detail information
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
     * delete OSS object
     *
     * @return content
     */
    @CliCommand(value = "rm", help = "Delete OSS object")
    public String rm(@CliOption(key = {""}, mandatory = true, help = "OSS file path") final String filePath) {
        try {
            if (filePath.endsWith("*")) {
                ObjectListing list = aliyunOssService.list(currentBucket, filePath.replace("*", ""));
                int size = list.getObjectSummaries().size();
                for (OSSObjectSummary objectSummary : list.getObjectSummaries()) {
                    aliyunOssService.delete(currentBucket, objectSummary.getKey());
                }
                if (size > 1) {
                    return size + " files deleted!";
                }
            } else {
                aliyunOssService.delete(currentBucket, filePath);
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return filePath + " deleted!";
    }

    /**
     * copy object
     *
     * @return content
     */
    @CliCommand(value = "cp", help = "Copy OSS project")
    public String cp(@CliOption(key = {"source"}, mandatory = true, help = "Source OSS file path") final String sourceFilePath,
                     @CliOption(key = {"dest"}, mandatory = true, help = "Destination OSS file path") final String destFilePath) {
        try {
            OSSUri sourceUri = new OSSUri(currentBucket, sourceFilePath);
            OSSUri destUri = new OSSUri(currentBucket, destFilePath);
            if (sourceFilePath.startsWith("oss://")) {
                sourceUri = new OSSUri(sourceFilePath);
            }
            if (destFilePath.startsWith("oss://")) {
                destUri = new OSSUri(destFilePath);
            }
            aliyunOssService.copy(sourceUri.getBucket(), sourceUri.getFilePath(), destUri.getBucket(), destUri.getFilePath());
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    /**
     * move object
     *
     * @return content
     */
    @CliCommand(value = "mv", help = "Move OSS project")
    public String mv(@CliOption(key = {"source"}, mandatory = true, help = "Source OSS file path") final String sourceFilePath,
                     @CliOption(key = {"dest"}, mandatory = true, help = "Destination OSS file path") final String destFilePath) {
        try {
            OSSUri sourceUri = new OSSUri(currentBucket, sourceFilePath);
            OSSUri destUri = new OSSUri(currentBucket, destFilePath);
            if (sourceFilePath.startsWith("oss://")) {
                sourceUri = new OSSUri(sourceFilePath);
            }
            if (destFilePath.startsWith("oss://")) {
                destUri = new OSSUri(destFilePath);
            }
            aliyunOssService.copy(sourceUri.getBucket(), sourceUri.getFilePath(), destUri.getBucket(), destUri.getFilePath());
            aliyunOssService.delete(sourceUri.getBucket(), sourceUri.getFilePath());
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    /**
     * switch bucket
     *
     * @return content
     */
    @CliCommand(value = "set", help = "Set object metadata")
    public String set(@CliOption(key = {"key"}, mandatory = true, help = "OSS file path") final String key,
                      @CliOption(key = {"value"}, mandatory = true, help = "Metadata key") final String value,
                      @CliOption(key = {""}, mandatory = true, help = "Metadata value") final String filePath) {
        try {
            aliyunOssService.setObjectMetadata(currentBucket, filePath, key, value);
        } catch (Exception e) {
            return e.getMessage();
        }
        return file(filePath);
    }
}
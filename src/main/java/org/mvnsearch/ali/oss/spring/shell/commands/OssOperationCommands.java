package org.mvnsearch.ali.oss.spring.shell.commands;

import com.aliyun.openservices.oss.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.http.impl.cookie.DateUtils;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
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
     * log
     */
    private Logger log = LoggerFactory.getLogger(OssOperationCommands.class);
    /**
     * current bucket
     */
    private OSSUri currentBucket = null;
    /**
     * local repository
     */
    private File localRepository;
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
        this.currentBucket = new OSSUri(configService.getProperty("BUCKET"), null);
        String repository = configService.getRepository();
        if (repository != null && !repository.isEmpty()) {
            localRepository = new File(repository);
        }
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "config", help = "Config the Aliyun OSS access info")
    public String config(@CliOption(key = {"id"}, mandatory = true, help = "Aliyun Access ID") final String accessId,
                         @CliOption(key = {"key"}, mandatory = true, help = "Aliyun Access Key") final String accessKey,
                         @CliOption(key = {"repository"}, mandatory = true, help = "Aliyun Access Key") final String reposity) {
        try {
            configService.setAccessInfo(accessId, accessKey);
            aliyunOssService.refreshToken();
            //local repository
            File temp = new File(reposity);
            if (!temp.exists()) {
                FileUtils.forceMkdir(temp);
            }
            localRepository = temp;
            configService.setRepository(reposity);
        } catch (Exception e) {
            log.error("config", e);
            return e.getMessage();
        }
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
    public String create(@CliOption(key = {"acl"}, mandatory = false, help = "Bucket ACL, such as: Private, R- or RW") String acl,
                         @CliOption(key = {""}, mandatory = true, help = "prefix wild matched file name") final String bucket) {
        try {
            if (acl == null || acl.isEmpty() || acl.equalsIgnoreCase("private")) {
                acl = "--";
            }
            if (!(acl.equalsIgnoreCase("--") || acl.equals("R-") || acl.equals("RW"))) {
                return "ACL value should be 'Private', 'R-' or 'RW'";
            }
            aliyunOssService.createBucket(bucket);
            aliyunOssService.setBucketACL(bucket, acl);
            use(bucket);
            return "Bucket 'oss://" + bucket + "' created and switched";
        } catch (Exception e) {
            log.error("create", e);
            return e.getMessage();
        }
    }

    /**
     * drop bucket
     *
     * @param bucket bucket
     * @return result
     */
    @CliCommand(value = "drop", help = "Drop bucket")
    public String drop(@CliOption(key = {""}, mandatory = true, help = "Bucket name") String bucket) {
        try {
            ObjectListing listing = aliyunOssService.list(bucket, "");
            if (!listing.getObjectSummaries().isEmpty()) {
                return "Bucket is not empty, and you can't delete it!";
            }
            aliyunOssService.dropBucket(bucket);
            if (bucket.equals(currentBucket.getBucket())) {
                currentBucket = null;
                configService.setProperty("BUCKET", null);
            }
            return bucket + " bucket dropped!";
        } catch (Exception e) {
            log.error("drop", e);
            return e.getMessage();
        }
    }

    /**
     * create new bucket
     *
     * @return new bucket
     */
    @CliCommand(value = "chmod", help = "Set Current Bucket Access Control List: Private, R- or RW")
    public String chmod(@CliOption(key = {""}, mandatory = true, help = "Set bucket Access Control List") String acl) {
        try {
            if (currentBucket == null) {
                return "Please select a bucket!";
            }
            if (acl != null && acl.equalsIgnoreCase("private")) {
                acl = "--";
            }
            if (acl != null && (acl.equalsIgnoreCase("--") || acl.equals("R-") || acl.equals("RW"))) {
                aliyunOssService.setBucketACL(currentBucket.getBucket(), acl);
            } else {
                return "ACL value should be 'Private', 'R-' or 'RW'";
            }
        } catch (Exception e) {
            log.error("chmod", e);
            return e.getMessage();
        }
        return currentBucket + "'s ACL: " + acl;
    }

    /**
     * download file
     *
     * @return content
     */
    @CliCommand(value = "get", help = "Get the file from OSS and save it to local disk")
    public String get(@CliOption(key = {"dest"}, mandatory = false, help = "destination file path on disk") String destFilePath,
                      @CliOption(key = {""}, mandatory = true, help = "source file path on OSS") String sourceFilePath) {
        if (currentBucket == null) {
            return "Please select a bucket!";
        }
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(sourceFilePath);
            if (destFilePath == null || destFilePath.isEmpty()) {
                destFilePath = objectUri.getPathInRepository(localRepository).getAbsolutePath();
            }
            return "Saved to " + aliyunOssService.get(objectUri, destFilePath);
        } catch (Exception e) {
            log.error("get", e);
            return e.getMessage();
        }
    }

    /**
     * display object content
     *
     * @return content
     */
    @CliCommand(value = "more", help = "Display OSS file content")
    public String more(@CliOption(key = {""}, mandatory = true, help = "destination file path on disk") final String sourceFilePath) {
        try {
            OSSUri sourceUri = currentBucket.getChildObjectUri(sourceFilePath);
            OSSObject ossObject = aliyunOssService.getOssObject(sourceUri);
            if (ossObject != null) {
                System.out.println(IOUtils.toString(ossObject.getObjectContent()));
            } else {
                return "Object NOT Found!";
            }
        } catch (Exception e) {
            log.error("more", e);
            return e.getMessage();
        }
        return null;
    }

    /**
     * open OSS object in browser
     *
     * @return content
     */
    @CliCommand(value = "open", help = "Open OSS object in Browser")
    public String open(@CliOption(key = {""}, mandatory = true, help = "OSS object uri or path") final String sourceFilePath) {
        try {
            OSSUri sourceUri = currentBucket.getChildObjectUri(sourceFilePath);
            //判断是否支持打开浏览器
            if (Desktop.isDesktopSupported()) {
                ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(sourceUri);
                if (objectMetadata != null) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(sourceUri.getHttpUrl()));
                } else {
                    return "Object NOT Found!";
                }
            } else {
                return "Luanch Brower not support, please copy url to visit: " + sourceUri.getBucket();
            }
        } catch (Exception e) {
            log.error("open", e);
            return e.getMessage();
        }
        return null;
    }

    /**
     * list files
     *
     * @return content
     */
    @CliCommand(value = "put", help = "Put the local file to OSS")
    public String put(@CliOption(key = {"source"}, mandatory = true, help = "source file path on disk") String sourceFilePath,
                      @CliOption(key = {""}, mandatory = true, help = "destination file path on OSS") String destFilePath) {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            return "File not extis: " + sourceFilePath;
        }
        try {
            if (sourceFile.isDirectory()) {
                int count = uploadDirectory(currentBucket.getBucket(), destFilePath, sourceFile, false);
                return count + " files uploaded";
            } else {
                if (destFilePath.endsWith("/")) {
                    destFilePath = destFilePath + sourceFile.getName();
                }
                OSSUri destObjectUri = currentBucket.getChildObjectUri(destFilePath);
                aliyunOssService.put(sourceFilePath, destObjectUri);
                return "Uploaded to: " + destObjectUri;
            }
        } catch (Exception e) {
            log.error("upt", e);
            return e.getMessage();
        }
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
    private int uploadDirectory(String bucket, String destFilePath, File sourceDir, boolean synced) throws Exception {
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
        int count = files.size();
        for (File file : files) {
            String destPath = file.getAbsolutePath().replace(sourceDir.getAbsolutePath(), "");
            destPath = destFilePath + destPath.replaceAll("\\\\", "/");
            if (destPath.contains("//")) {
                destPath = destPath.replace("//", "/");
            }
            boolean overwrite = true;
            OSSUri objectUri = new OSSUri(bucket, destPath);
            //sync validation
            if (synced) {
                ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(objectUri);
                if (objectMetadata != null) {
                    if (objectMetadata.getLastModified().getTime() >= file.lastModified() && file.length() == objectMetadata.getContentLength()) {
                        overwrite = false;
                    }
                }
            }
            if (overwrite) {
                aliyunOssService.put(file.getAbsolutePath(), objectUri);
                System.out.println("Uploaded: " + objectUri);
            } else {
                System.out.println("Skipped:  o" + objectUri);
                count = count - 1;
            }
        }
        return count;
    }

    /**
     * sync directory
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
                int count = uploadDirectory(currentBucket.getBucket(), destPath, sourceFile, true);
                return count + " files uploaded!";
            } else {
                OSSUri objectUri = currentBucket.getChildObjectUri(destPath);
                aliyunOssService.put(sourceDirectory, objectUri);
                return "Uploaded " + objectUri;
            }
        } catch (Exception e) {
            log.error("sync", e);
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
            ObjectListing objectListing = aliyunOssService.list(currentBucket.getBucket(), prefix);
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                buf.append(DateUtils.formatDate(objectSummary.getLastModified(), "yyyy-MM-dd HH:mm:ss") +
                        StringUtils.padLeft(String.valueOf(objectSummary.getSize()), 10, ' ') + " " +
                        objectSummary.getKey() + StringUtils.LINE_SEPARATOR);
            }
            buf.append(objectListing.getObjectSummaries().size() + " files found");
        } catch (Exception e) {
            log.error("ls", e);
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
                buf.append((bucket.getName().equals(currentBucket.getBucket())) ? " => " : "    ");
                //apend url
                buf.append("oss://" + bucket.getName() + StringUtils.LINE_SEPARATOR);
            }
        } catch (Exception e) {
            log.error("listbuckets", e);
            return e.getMessage();
        }
        return buf.toString().trim();
    }

    /**
     * change directory
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
        this.currentBucket = new OSSUri(bucketName, null);
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
            ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(currentBucket.getChildObjectUri(filePath));
            if (objectMetadata != null) {
                Map<String, Object> rawMetadata = objectMetadata.getRawMetadata();
                List<String> reservedKeys = Arrays.asList("Connection", "Server", "x-oss-request-id");
                for (Map.Entry<String, Object> entry : rawMetadata.entrySet()) {
                    if (!reservedKeys.contains(entry.getKey())) {
                        buf.append(StringUtils.padRight(entry.getKey(), 20, ' ') + " : " + entry.getValue() + StringUtils.LINE_SEPARATOR);
                    }
                }
                Map<String, String> userMetadata = objectMetadata.getUserMetadata();
                if (userMetadata != null && !userMetadata.isEmpty()) {
                    buf.append("================ User Metadata ========================" + StringUtils.LINE_SEPARATOR);
                    for (Map.Entry<String, String> entry : userMetadata.entrySet()) {
                        buf.append(StringUtils.padRight(entry.getKey(), 20, ' ') + " : " + entry.getValue() + StringUtils.LINE_SEPARATOR);
                    }
                }
            } else {
                return "Object Not Found!";
            }
        } catch (Exception e) {
            log.error("file", e);
            return e.getMessage();
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
                ObjectListing list = aliyunOssService.list(currentBucket.getBucket(), filePath.replace("*", ""));
                int size = list.getObjectSummaries().size();
                for (OSSObjectSummary objectSummary : list.getObjectSummaries()) {
                    aliyunOssService.delete(currentBucket.getChildObjectUri(objectSummary.getKey()));
                }
                if (size > 1) {
                    return size + " files deleted!";
                }
            } else {
                aliyunOssService.delete(currentBucket.getChildObjectUri(filePath));
            }
        } catch (Exception e) {
            log.error("rm", e);
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
            OSSUri sourceUri = currentBucket.getChildObjectUri(sourceFilePath);
            OSSUri destUri = currentBucket.getChildObjectUri(destFilePath);
            if (sourceFilePath.startsWith("oss://")) {
                sourceUri = new OSSUri(sourceFilePath);
            }
            if (destFilePath.startsWith("oss://")) {
                destUri = new OSSUri(destFilePath);
            }
            aliyunOssService.copy(sourceUri, destUri);
        } catch (Exception e) {
            log.error("cp", e);
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
            OSSUri sourceUri = currentBucket.getChildObjectUri(sourceFilePath);
            OSSUri destUri = currentBucket.getChildObjectUri(destFilePath);
            aliyunOssService.copy(sourceUri, destUri);
            aliyunOssService.delete(sourceUri);
        } catch (Exception e) {
            log.error("mv", e);
            return e.getMessage();
        }
        return null;
    }

    /**
     * set object meta data
     *
     * @return content
     */
    @CliCommand(value = "set", help = "Set object metadata")
    public String set(@CliOption(key = {"key"}, mandatory = true, help = "OSS file path") final String key,
                      @CliOption(key = {"value"}, mandatory = true, help = "Metadata key") final String value,
                      @CliOption(key = {""}, mandatory = true, help = "Metadata value") final String filePath) {
        try {
            aliyunOssService.setObjectMetadata(currentBucket.getChildObjectUri(filePath), key, value);
        } catch (Exception e) {
            log.error("set", e);
            return e.getMessage();
        }
        return file(filePath);
    }
}
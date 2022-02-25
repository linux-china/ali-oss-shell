package org.mvnsearch.ali.oss.spring.shell.commands;

import com.aliyun.oss.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.impl.cookie.DateUtils;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.ali.oss.spring.services.*;
import org.mvnsearch.ali.oss.spring.shell.converters.BucketEnum;
import org.mvnsearch.ali.oss.spring.shell.converters.HttpHeader;
import org.mvnsearch.ali.oss.spring.shell.converters.ObjectKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * Aliyun OSS operation commands
 *
 * @author linux_china
 */
@ShellComponent
public class OssOperationCommands {
    /**
     * log
     */
    private static final Logger log = LoggerFactory.getLogger(OssOperationCommands.class);
    /**
     * The platform-specific line separator.
     */
    public static final String LINE_SEPARATOR = SystemUtils.LINE_SEPARATOR;
    /**
     * current bucket
     */
    public static OSSUri currentBucket = null;
    /**
     * local repository
     */
    private File localRepository;
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
        currentBucket = new OSSUri(configService.getProperty("BUCKET"), null);
        String repository = configService.getRepository();
        if (repository != null && !repository.isEmpty()) {
            localRepository = new File(repository);
        }
        //bucket常量注入，用于提示
        if (configService.available()) {
            try {
                List<Bucket> buckets = aliyunOssService.getBuckets();
                for (Bucket bucket : buckets) {
                    BucketEnum.addBucketName(bucket.getName());
                }
            } catch (Exception ignore) {

            }
        }
    }

    /**
     * config command to save aliyun OSS information
     *
     * @return result
     */
    @ShellMethod(key = "config", value = "Config the Aliyun OSS access info")
    public String config(@ShellOption(value = {"id"}, help = "Aliyun Access ID") @NotNull String accessId,
                         @ShellOption(value = {"key"}, help = "Aliyun Access Key") @NotNull String accessKey,
                         @ShellOption(value = {"repository"}, help = "local repository directory") @NotNull File repository) {
        try {
            configService.setAccessInfo(accessId, accessKey);
            aliyunOssService.refreshToken();
            try {
                List<Bucket> buckets = aliyunOssService.getBuckets();
                //local repository
                if (!repository.exists()) {
                    FileUtils.forceMkdir(repository);
                    //create bucket directory
                    if (buckets != null) {
                        for (Bucket bucket : buckets) {
                            FileUtils.forceMkdir(new File(repository, bucket.getName()));
                        }
                    }
                }
                localRepository = repository;
                configService.setRepository(repository.getAbsolutePath());
            } catch (Exception e) {
                System.out.println(wrappedAsRed("Failed to set access info!"));
                return null;
            }
        } catch (Exception e) {
            log.error("config", e);
            return wrappedAsRed(e.getMessage());
        }
        return "Access info saved!";
    }

    /**
     * list all your buckets
     *
     * @return bucket list
     */
    @ShellMethod(key = "df", value = "List all your buckets")
    public String df() {
        return listBuckets();
    }

    /**
     * create a bucket
     *
     * @return new bucket
     */
    @ShellMethod(key = "create", value = "Create a new bucket")
    public String create(@ShellOption(value = {"acl"}, help = "Bucket's acl, such as: Private, ReadOnly or ReadWrite") BucketAclType acl,
                         @ShellOption(value = {""}, help = "Bucket name: pattern as [a-z][a-z0-9\\-_]{5,15}") @NotNull String bucket) {
        try {

            aliyunOssService.createBucket(bucket);
            if (acl != null && acl.getType() != null) {
                aliyunOssService.setBucketACL(bucket, acl.getShortCode());
            }
            BucketEnum.addBucketName(bucket);
            if (localRepository != null) {
                FileUtils.forceMkdir(new File(localRepository, bucket));
            }
            return MessageFormat.format("Bucket ''{0}'' has been created!", bucket);
        } catch (Exception e) {
            log.error("create", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * drop bucket
     *
     * @param bucketEnum bucket enum
     * @return result
     */
    @ShellMethod(key = "drop", value = "Drop bucket")
    public String drop(@ShellOption(value = {""}, help = "Bucket's name") @NotNull BucketEnum bucketEnum) {
        try {
            String bucketName = bucketEnum.getName();
            Bucket bucket = aliyunOssService.getBucket(bucketName);
            if (bucket != null) {
                return wrappedAsRed(MessageFormat.format("Bucket ''{0}'' not found!", bucketName));
            }
            ObjectListing listing = aliyunOssService.list(bucketName, "");
            if (!listing.getObjectSummaries().isEmpty()) {
                return wrappedAsRed("The bucket is not empty, and you can't delete it!");
            }
            aliyunOssService.dropBucket(bucketName);
            if (bucketName.equals(currentBucket.getBucket())) {
                currentBucket = null;
                configService.setProperty("BUCKET", null);
            }
            return MessageFormat.format("Bucket ''{0}'' has been dropped!", bucketName);
        } catch (Exception e) {
            log.error("drop", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * Change current bucket's Access Control Lists
     *
     * @return result
     */
    @ShellMethod(key = "chmod", value = "Change current bucket's Access Control Lists")
    public String chmod(@ShellOption(value = {""}, help = "Access Control List: Private, ReadOnly or ReadWrite") @NotNull BucketAclType acl) {
        try {
            if (currentBucket == null) {
                return wrappedAsYellow("Please select a bucket!");
            }
            if (acl != null && acl.getType() != null) {
                aliyunOssService.setBucketACL(currentBucket.getBucket(), acl.getShortCode());
            } else {
                return wrappedAsRed("ACL value should be 'Private', 'ReadOnly' or 'ReadWrite'.");
            }
            return MessageFormat.format("{0} {1}", acl, currentBucket.toString());
        } catch (Exception e) {
            log.error("chmod", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * download oss object
     *
     * @param localFilePath dest local file path
     * @param objectKey     object key
     * @return message
     */
    @ShellMethod(key = "get", value = "Retrieve OSS object and save it to local file system")
    public String get(@ShellOption(value = {"o"}, help = "Local file or directory path") File localFilePath,
                      @ShellOption(value = {""}, help = "OSS object uri or key") @NotNull ObjectKey objectKey) {
        if (currentBucket == null) {
            return wrappedAsYellow("Please select a bucket!");
        }
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(objectKey.getKey());
            ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(objectUri);
            if (objectMetadata == null) {
                return wrappedAsRed("The object not found!");
            }
            if (localFilePath == null) {
                localFilePath = objectUri.getPathInRepository(localRepository);
            }
            String destFilePath = aliyunOssService.get(objectUri, localFilePath.getAbsolutePath());
            return MessageFormat.format("Object {0} saved to {1} ({3} bytes)", objectUri.toString(), destFilePath, objectMetadata.getContentLength());
        } catch (Exception e) {
            log.error("get", e);
            return e.getMessage();
        }
    }

    /**
     * display object content
     *
     * @return message
     */
    @ShellMethod(key = "cat", value = "concatenate and print OSS object's content")
    public String cat(@ShellOption(value = {""}, help = "OSS object uri or key") @NotNull ObjectKey objectKey) {
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(objectKey.getKey());
            OSSObject ossObject = aliyunOssService.getOssObject(objectUri);
            if (ossObject != null) {
                byte[] content = IOUtils.toByteArray(ossObject.getObjectContent());
                if ("gzip".equalsIgnoreCase(ossObject.getObjectMetadata().getContentEncoding())) {
                    content = ZipUtils.uncompress(content);
                }
                System.out.println(new String(content, StandardCharsets.UTF_8));
            } else {
                return wrappedAsRed("The object not found!");
            }
        } catch (Exception e) {
            log.error("cat", e);
            return wrappedAsRed(e.getMessage());
        }
        return null;
    }

    /**
     * open OSS object in browser
     *
     * @param objectKey object key or uri
     * @return message
     */
    @ShellMethod(key = "open", value = "Open OSS object in browser")
    public String open(@ShellOption(value = {""}, help = "OSS object uri or key") @NotNull ObjectKey objectKey) {
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(objectKey.getKey());
            //判断是否支持打开浏览器
            if (Desktop.isDesktopSupported()) {
                ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(objectUri);
                if (objectMetadata != null) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(objectUri.getHttpUrl()));
                } else {
                    return wrappedAsRed("The object not found!");
                }
            } else {
                return wrappedAsRed("Luanch Brower not support, please copy url to browser address: " + objectUri.getBucket());
            }
        } catch (Exception e) {
            log.error("open", e);
            return wrappedAsRed(e.getMessage());
        }
        return null;
    }

    /**
     * Upload file or director to OSS
     *
     * @return message
     */
    @ShellMethod(key = "put", value = "Upload the local file or directory to OSS")
    public String put(@ShellOption(value = {"source"}, help = "Local file or directory path") @NotNull File sourceFile,
                      @ShellOption(value = {"zip"}, help = "Zip the file", defaultValue = "false") Boolean zip,
                      @ShellOption(value = {""}, help = "Destination OSS object uri, key or path") String objectKey) {
        if (!sourceFile.exists()) {
            return wrappedAsRed(MessageFormat.format("The file ''{0}'' not exits. ", sourceFile.getAbsolutePath()));
        }
        try {
            if (sourceFile.isDirectory()) {
                int count = uploadDirectory(currentBucket.getBucket(), StringUtils.defaultIfEmpty(objectKey, ""), sourceFile, false, zip);
                return count + " files uploaded";
            } else {
                if (objectKey == null || objectKey.isEmpty()) {
                    objectKey = sourceFile.getName();
                }
                if (objectKey.endsWith("/")) {
                    objectKey = objectKey + sourceFile.getName();
                }
                OSSUri destObjectUri = currentBucket.getChildObjectUri(objectKey);
                ObjectMetadata metadata = aliyunOssService.put(sourceFile.getAbsolutePath(), destObjectUri, zip);
                return MessageFormat.format("File ''{0}'' stored as {1} ({2} bytes)",
                        sourceFile.getAbsolutePath(), destObjectUri.toString(), metadata.getContentLength());
            }
        } catch (Exception e) {
            log.error("put", e);
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
    private int uploadDirectory(String bucket, String destFilePath, File sourceDir, boolean synced, Boolean zip) throws Exception {
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
                aliyunOssService.put(file.getAbsolutePath(), objectUri, zip);
                System.out.println("Uploaded: " + objectUri);
            } else {
                System.out.println("Skipped: " + objectUri);
                count = count - 1;
            }
        }
        return count;
    }

    /**
     * sync directory
     *
     * @return message
     */
    @ShellMethod(key = "sync", value = "Sync bucket or directory with OSS")
    public String sync(@ShellOption(value = {"source"}, help = "local directory") @Nullable File sourceFile,
                       @ShellOption(value = {"bucket"}, help = "bucket name") @Nullable BucketEnum bucketEnum,
                       @ShellOption(value = {"zip"}, help = "GZip the file", defaultValue = "false") Boolean zip,
                       @ShellOption(value = {""}, help = "OSS object path") String objectPath) {
        if (currentBucket == null) {
            return wrappedAsYellow("Please select a bucket!");
        }
        String bucketName = currentBucket.getBucket();
        if (sourceFile == null && bucketEnum == null) {
            return wrappedAsYellow("Please use --bucket or --source for sync");
        }
        //如果source file为空，进行bucket同步，同时忽略object path
        if (sourceFile == null) {
            sourceFile = new File(localRepository, bucketEnum.getName());
            bucketName = bucketEnum.getName();
            objectPath = "";
        }
        if (!sourceFile.exists()) {
            return wrappedAsRed(MessageFormat.format("File ''{0}'' not exits: ", sourceFile.getAbsolutePath()));
        }
        try {
            if (sourceFile.isDirectory()) {
                int count = uploadDirectory(bucketName, StringUtils.defaultIfEmpty(objectPath, ""), sourceFile, true, zip);
                return count + " files uploaded!";
            } else {
                OSSUri objectUri = currentBucket.getChildObjectUri(objectPath);
                ObjectMetadata metadata = aliyunOssService.put(sourceFile.getAbsolutePath(), objectUri);
                return MessageFormat.format("File '{0}' stored as {1} ({2} bytes)",
                        sourceFile.getAbsolutePath(), objectUri.toString(), metadata.getContentLength());
            }
        } catch (Exception e) {
            log.error("sync", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * list files
     *
     * @return content
     */
    @ShellMethod(key = "ls", value = "List object or virtual directory in Bucket")
    public String ls(@ShellOption(value = "", help = "Object key or path: support suffix wild match", defaultValue = "*") String objectPath) {
        if (currentBucket == null) {
            return listBuckets();
        }
        StringBuilder buf = new StringBuilder();
        OSSUri dirObject = currentBucket.getChildObjectUri(objectPath);
        try {
            ObjectListing objectListing;
            if (dirObject.getFilePath().endsWith("*")) {
                objectListing = aliyunOssService.list(currentBucket.getBucket(), dirObject.getFilePath());
            } else {
                objectListing = aliyunOssService.listChildren(currentBucket.getBucket(), dirObject.getFilePath());
            }
            int dirCount = 0;
            int objectCount = 0;
            if (!objectListing.getCommonPrefixes().isEmpty()) {
                for (String commonPrefix : objectListing.getCommonPrefixes()) {
                    buf.append(StringUtils.repeat("-.", 14) + "- " + commonPrefix + LINE_SEPARATOR);
                    dirCount += 1;
                }
            }
            if (!objectListing.getObjectSummaries().isEmpty()) {
                for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    final String objectKey = objectSummary.getKey();

                    buf.append(DateUtils.formatDate(objectSummary.getLastModified(), "yyyy-MM-dd HH:mm:ss") +
                            StringUtils.leftPad(String.valueOf(objectSummary.getSize()), 10, ' ') + " " +
                            objectKey + LINE_SEPARATOR);
                    objectCount += 1;
                }
            }
            if (dirCount > 0 && objectCount > 0) {
                buf.append(dirCount + " virtual directories and " + objectCount + " objects found!");
            } else if (dirCount > 0) {
                buf.append(dirCount + " virtual directories found!");
            } else if (objectCount > 0) {
                buf.append(objectCount + " objects found!");
            } else {
                buf.append("No object found for " + dirObject.toString());
            }
            return buf.toString();
        } catch (Exception e) {
            log.error("ls", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * list buckets
     *
     * @return bucket list
     */
    private String listBuckets() {
        StringBuilder buf = new StringBuilder();
        try {
            buf.append("Buckets:" + LINE_SEPARATOR);
            List<Bucket> buckets = aliyunOssService.getBuckets();
            for (Bucket bucket : buckets) {
                //create time
                buf.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bucket.getCreationDate()));
                //pad
                buf.append((bucket.getName().equals(currentBucket.getBucket())) ? " => " : "    ");
                //append url
                buf.append("oss://" + bucket.getName() + LINE_SEPARATOR);
            }
        } catch (Exception e) {
            log.error("listbuckets", e);
            return wrappedAsRed(e.getMessage());
        }
        return buf.toString().trim();
    }

    /**
     * change directory
     *
     * @return content
     */
    @ShellMethod(key = "cd", value = "Change virtual directory on OSS")
    public String cd(@ShellOption(value = {""}, help = "Oss virtual directory name") String dir) {
        if (dir == null || dir.isEmpty() || dir.equals("/")) {
            currentBucket.setFilePath("");
        } else {
            if (dir.equals(".")) {
                return currentBucket.toString();
            }
            if (!dir.endsWith("/")) {
                dir = dir + "/";
            }
            String currentDir = currentBucket.getChildObjectUri(dir).getFilePath();
            currentBucket.setFilePath(currentDir);
        }
        OssCliPromptProvider.prompt = currentBucket.toString();
        return currentBucket.toString();
    }

    /**
     * display working virtual directory uri
     *
     * @return content
     */
    @ShellMethod(key = "pwd", value = "Return working virtual directory uri")
    public String pwd() {
        if (currentBucket == null) {
            return wrappedAsRed("Please select a bucket!");
        }
        return wrappedAsRed(currentBucket.toString());
    }

    /**
     * switch bucket
     *
     * @return content
     */
    @ShellMethod(key = "use", value = "Switch bucket")
    public String use(@ShellOption(value = {""}, help = "bucket name") @NotNull BucketEnum bucketEnum) {
        try {
            String bucketName = bucketEnum.getName();
            Bucket bucket = aliyunOssService.getBucket(bucketName);
            if (bucket == null) {
                return wrappedAsRed("The bucket not found");
            }
            currentBucket = new OSSUri(bucketName, null);
            configService.setProperty("BUCKET", bucketName);
            OssCliPromptProvider.prompt = currentBucket.toString();
            return "Switched to " + currentBucket.toString();
        } catch (Exception e) {
            log.error("use", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * show OSS object detail information
     *
     * @return content
     */
    @ShellMethod(key = "file", value = "Get OSS object detail information")
    public String file(@ShellOption(value = {""}, help = "Oss object uri or key") @NotNull ObjectKey objectKey) {
        StringBuilder buf = new StringBuilder();
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(objectKey.getKey());
            ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(objectUri);
            if (objectMetadata != null) {
                buf.append(StringUtils.rightPad("Bucket", 20, ' ') + " : " + objectUri.getBucket() + LINE_SEPARATOR);
                buf.append(StringUtils.rightPad("Folder", 20, ' ') + " : " + objectUri.getPath() + LINE_SEPARATOR);
                buf.append(StringUtils.rightPad("Name", 20, ' ') + " : " + objectUri.getFileName() + LINE_SEPARATOR);
                Map<String, Object> rawMetadata = objectMetadata.getRawMetadata();
                //date
                buf.append(StringUtils.rightPad("Date", 20, ' ') + " : " + rawMetadata.get("Date") + LINE_SEPARATOR);
                buf.append(StringUtils.rightPad("Last-Modified", 20, ' ') + " : " + rawMetadata.get("Date") + LINE_SEPARATOR);
                if (rawMetadata.get("Expires") != null) {
                    buf.append(StringUtils.rightPad("Expires", 20, ' ') + " : " +
                            rawMetadata.get("Expires") + LINE_SEPARATOR);
                }
                //content
                buf.append(StringUtils.rightPad("Content-Type", 20, ' ') + " : " + rawMetadata.get("Content-Type") + LINE_SEPARATOR);
                buf.append(StringUtils.rightPad("Content-Length", 20, ' ') + " : " + rawMetadata.get("Content-Length") + LINE_SEPARATOR);
                List<String> reservedKeys = Arrays.asList("Connection", "Server", "x-oss-request-id", "Date", "Last-Modified", "Content-Type", "Content-Length");
                for (Map.Entry<String, Object> entry : rawMetadata.entrySet()) {
                    if (!reservedKeys.contains(entry.getKey())) {
                        buf.append(StringUtils.rightPad(entry.getKey(), 20, ' ') + " : " + entry.getValue() + LINE_SEPARATOR);
                    }
                }
                Map<String, String> userMetadata = objectMetadata.getUserMetadata();
                if (userMetadata != null && !userMetadata.isEmpty()) {
                    buf.append("================ User Metadata ========================" + LINE_SEPARATOR);
                    for (Map.Entry<String, String> entry : userMetadata.entrySet()) {
                        buf.append(StringUtils.rightPad(entry.getKey(), 20, ' ') + " : " + entry.getValue() + LINE_SEPARATOR);
                    }
                }
            } else {
                return wrappedAsRed("The object not found!");
            }
        } catch (Exception e) {
            log.error("file", e);
            return wrappedAsRed(e.getMessage());
        }
        return buf.toString().trim();
    }

    /**
     * share object to generate signed url, expired one hour
     *
     * @return content
     */
    @ShellMethod(key = "share", value = "Generate signed url for OSS object.")
    public String share(@ShellOption(value = {""}, help = "Object uri or key") @NotNull ObjectKey objectKey) {
        OSSUri destObject = currentBucket.getChildObjectUri(objectKey.getKey());
        try {
            URL url = aliyunOssService.getOssClient().generatePresignedUrl(destObject.getBucket(), destObject.getFilePath(),
                    new Date(System.currentTimeMillis() + 1000 * 60 * 60));
            return url.toString();
        } catch (Exception e) {
            log.error("share", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * delete OSS object
     *
     * @return content
     */
    @ShellMethod(key = "rm", value = "Delete OSS object")
    public String rm(@ShellOption(value = {""}, help = "OSS object uri or key: support suffix wild match") @NotNull ObjectKey objectKey) {
        try {
            String filePath = objectKey.getKey();
            if (filePath.endsWith("*") || filePath.endsWith("/")) {
                ObjectListing list = aliyunOssService.list(currentBucket.getBucket(), filePath);
                int size = list.getObjectSummaries().size();
                for (OSSObjectSummary objectSummary : list.getObjectSummaries()) {
                    OSSUri objectToDeleted = currentBucket.getChildObjectUri(objectSummary.getKey());
                    aliyunOssService.delete(objectToDeleted);
                    System.out.println("Deleted: " + objectToDeleted.toString());
                }
                if (size > 1) {
                    return size + " objects deleted!";
                } else {
                    return "1 object deleted!";
                }
            } else {
                OSSUri destObject = currentBucket.getChildObjectUri(filePath);
                aliyunOssService.delete(destObject);
                return "Deleted: " + destObject.toString();
            }
        } catch (Exception e) {
            log.error("rm", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * copy object
     *
     * @return content
     */
    @ShellMethod(key = "cp", value = "Copy OSS object")
    public String cp(@ShellOption(value = {"object"}, help = "Source object key or uri") @NotNull ObjectKey sourceObjectKey,
                     @ShellOption(value = {"dest"}, help = "Dest object key") @NotNull String destFilePath) {
        try {
            OSSUri sourceUri = currentBucket.getChildObjectUri(sourceObjectKey.getKey());
            OSSUri destUri = currentBucket.getChildObjectUri(destFilePath);
            aliyunOssService.copy(sourceUri, destUri);
            return MessageFormat.format("''{0}'' has been copied to ''{1}''", sourceUri.toString(), destUri.toString());
        } catch (Exception e) {
            log.error("cp", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * move object
     *
     * @return content
     */
    @ShellMethod(key = "mv", value = "Move OSS Object")
    public String mv(@ShellOption(value = {"object"}, help = "Source object key") @NotNull ObjectKey sourceObjectKey,
                     @ShellOption(value = {""}, help = "Dest object key") @NotNull String destFilePath) {
        try {
            OSSUri sourceUri = currentBucket.getChildObjectUri(sourceObjectKey.getKey());
            OSSUri destUri = currentBucket.getChildObjectUri(destFilePath);
            aliyunOssService.copy(sourceUri, destUri);
            aliyunOssService.delete(sourceUri);
            return MessageFormat.format("''{0}'' has been moved to ''{1}''", sourceUri.toString(), destUri.toString());
        } catch (Exception e) {
            log.error("mv", e);
            return e.getMessage();
        }
    }

    /**
     * set object meta data
     *
     * @return content
     */
    @ShellMethod(key = "set", value = "Set object metadata")
    public String set(@ShellOption(value = {"key"}, help = "Metadata key") @NotNull HttpHeader httpHeader,
                      @ShellOption(value = {"value"}, help = "Metadata value") @NotNull String value,
                      @ShellOption(value = {""}, help = "Object key") @NotNull ObjectKey objectKey) {
        try {
            String key = httpHeader.getName();
            aliyunOssService.setObjectMetadata(currentBucket.getChildObjectUri(objectKey.getKey()), key, value);
        } catch (Exception e) {
            log.error("set", e);
            return e.getMessage();
        }
        return file(new ObjectKey(objectKey.getKey()));
    }

    /**
     * wrapped as red with Jansi
     *
     * @param text text
     * @return wrapped text
     */
    private String wrappedAsRed(String text) {
        return Ansi.ansi().fg(Ansi.Color.RED).a(text).toString();
    }


    /**
     * wrapped as yellow with Jansi
     *
     * @param text text
     * @return wrapped text
     */
    private String wrappedAsYellow(String text) {
        return Ansi.ansi().fg(Ansi.Color.YELLOW).a(text).toString();
    }

}
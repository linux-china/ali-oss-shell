package org.mvnsearch.ali.oss.spring.shell.commands;

import com.aliyun.openservices.oss.model.*;
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
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

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
    @CliCommand(value = "config", help = "Config the Aliyun OSS access settings")
    public String config(@CliOption(key = {"id"}, mandatory = true, help = "Aliyun Access ID") final String accessId,
                         @CliOption(key = {"key"}, mandatory = true, help = "Aliyun Access Key") final String accessKey,
                         @CliOption(key = {"repository"}, mandatory = true, help = "local repository directory") final File reposity) {
        try {
            configService.setAccessInfo(accessId, accessKey);
            aliyunOssService.refreshToken();
            try {
                List<Bucket> buckets = aliyunOssService.getBuckets();
                //local repository
                if (!reposity.exists()) {
                    FileUtils.forceMkdir(reposity);
                    //create bucket directory
                    if (buckets != null) {
                        for (Bucket bucket : buckets) {
                            FileUtils.forceMkdir(new File(reposity, bucket.getName()));
                        }
                    }
                }
                localRepository = reposity;
                configService.setRepository(reposity.getAbsolutePath());
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
    @CliCommand(value = "df", help = "List all your buckets")
    public String df() {
        return listBuckets();
    }

    /**
     * create a bucket
     *
     * @return new bucket
     */
    @CliCommand(value = "create", help = "Create a bucket")
    public String create(@CliOption(key = {"acl"}, mandatory = false, help = "Bucket's acl, such as: Private, R- or RW") String acl,
                         @CliOption(key = {""}, mandatory = true, help = "Bucket name: pattern as [a-z][a-z0-9\\-_]{5,15}") final String bucket) {
        try {
            if (acl == null || acl.isEmpty() || acl.equalsIgnoreCase("private")) {
                acl = "--";
            }
            if (!(acl.equalsIgnoreCase("--") || acl.equals("R-") || acl.equals("RW"))) {
                return wrappedAsRed("ACL's value should be 'Private', 'R-' or 'RW'");
            }
            aliyunOssService.createBucket(bucket);
            aliyunOssService.setBucketACL(bucket, acl);
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
    @CliCommand(value = "drop", help = "Drop bucket")
    public String drop(@CliOption(key = {""}, mandatory = true, help = "Bucket name") BucketEnum bucketEnum) {
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
    @CliCommand(value = "chmod", help = "Change current bucket's Access Control Lists")
    public String chmod(@CliOption(key = {""}, mandatory = true, help = "Access Control List: Private, ReadOnly or ReadWrite") BucketAclType acl) {
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
    @CliCommand(value = "get", help = "Retrieve OSS object and save it to local file system")
    public String get(@CliOption(key = {"o"}, mandatory = false, help = "Local file or directory path") File localFilePath,
                      @CliOption(key = {""}, mandatory = true, help = "OSS object uri or key") String objectKey) {
        if (currentBucket == null) {
            return wrappedAsYellow("Please select a bucket!");
        }
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(objectKey);
            ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(objectUri);
            if (objectMetadata == null) {
                return wrappedAsRed("The object not found!");
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
    @CliCommand(value = "cat", help = "concatenate and print OSS object's content")
    public String cat(@CliOption(key = {""}, mandatory = true, help = "OSS object uri or key") final ObjectKey objectKey) {
        try {
            OSSUri objectUri = currentBucket.getChildObjectUri(objectKey.getKey());
            OSSObject ossObject = aliyunOssService.getOssObject(objectUri);
            if (ossObject != null) {
                byte[] content = IOUtils.toByteArray(ossObject.getObjectContent());
                if ("gzip".equalsIgnoreCase(ossObject.getObjectMetadata().getContentEncoding())) {
                    content = ZipUtils.uncompress(content);
                }
                System.out.println(new String(content));
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
    @CliCommand(value = "open", help = "Open OSS object in browser")
    public String open(@CliOption(key = {""}, mandatory = true, help = "OSS object uri or key") final ObjectKey objectKey) {
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
    @CliCommand(value = "put", help = "Upload the local file or directory to OSS")
    public String put(@CliOption(key = {"source"}, mandatory = true, help = "Local file or directory path") File sourceFile,
                      @CliOption(key = {"zip"}, mandatory = false, help = "Zip the file", specifiedDefaultValue = "true") Boolean zip,
                      @CliOption(key = {""}, mandatory = false, help = "Destination OSS object uri, key or path") String objectKey) {
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
    @CliCommand(value = "sync", help = "Sync bucket or directory with OSS")
    public String sync(@CliOption(key = {"source"}, mandatory = false, help = "local directory") @Nullable File sourceFile,
                       @CliOption(key = {"bucket"}, mandatory = false, help = "bucket name") @Nullable BucketEnum bucketEnum,
                       @CliOption(key = {"zip"}, mandatory = false, help = "Zip the file", specifiedDefaultValue = "true") Boolean zip,
                       @CliOption(key = {""}, mandatory = false, help = "OSS object path") String objectPath) {
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
    @CliCommand(value = "ls", help = "List object or virtual directory in Bucket")
    public String ls(@CliOption(key = {""}, mandatory = false, help = "Object key or path: support suffix wild match") final String filename) {
        if (currentBucket == null) {
            return listBuckets();
        }
        StringBuilder buf = new StringBuilder();
        OSSUri dirObject = currentBucket.getChildObjectUri(filename);
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
                    buf.append(DateUtils.formatDate(objectSummary.getLastModified(), "yyyy-MM-dd HH:mm:ss") +
                            StringUtils.leftPad(String.valueOf(objectSummary.getSize()), 10, ' ') + " " +
                            objectSummary.getKey() + LINE_SEPARATOR);
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
                //acl
                buf.append(aliyunOssService.getBucketACL(bucket.getName()));
                //create time
                buf.append("  " + DateUtils.formatDate(bucket.getCreationDate(), "yyyy-MM-dd HH:mm:ss"));
                //pad
                buf.append((bucket.getName().equals(currentBucket.getBucket())) ? " => " : "    ");
                //apend url
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
    @CliCommand(value = "cd", help = "Change virtual directory on OSS")
    public String cd(@CliOption(key = {""}, mandatory = false, help = "Oss virtual directory name") String dir) {
        if (dir == null || dir.isEmpty() || dir.equals("/")) {
            currentBucket.setFilePath("");
        } else {
            if (dir.endsWith(".")) {
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
    @CliCommand(value = "pwd", help = "Return working virtual directory uri")
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
    @CliCommand(value = "use", help = "Switch bucket")
    public String use(@CliOption(key = {""}, mandatory = true, help = "bucket name") final BucketEnum bucketEnum) {
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
    @CliCommand(value = "file", help = "Get OSS object detail information")
    public String file(@CliOption(key = {""}, mandatory = true, help = "Oss object uri or key") final ObjectKey objectKey) {
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
    @CliCommand(value = "share", help = "Generate signed url for OSS object.")
    public String share(@CliOption(key = {""}, mandatory = true, help = "Object uri or key") final String filePath) {
        OSSUri destObject = currentBucket.getChildObjectUri(filePath);
        try {
            URL url = aliyunOssService.getOssClient().generatePresignedUrl(destObject.getBucket(), destObject.getFilePath(),
                    new Date(System.currentTimeMillis() + 1000 * 60 * 60));
            return url.toString();
        } catch (Exception e) {
            log.error("share", e);
            return e.getMessage();
        }
    }

    /**
     * delete OSS object
     *
     * @return content
     */
    @CliCommand(value = "rm", help = "Delete OSS object")
    public String rm(@CliOption(key = {""}, mandatory = true, help = "OSS object uri or key: support suffix wild match") final ObjectKey objectKey) {
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
    @CliCommand(value = "cp", help = "Copy OSS object")
    public String cp(@CliOption(key = {"object"}, mandatory = true, help = "Source object key or uri") final ObjectKey sourceObjectKey,
                     @CliOption(key = {"dest"}, mandatory = true, help = "Dest object key") final String destFilePath) {
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
    @CliCommand(value = "mv", help = "Move OSS Object")
    public String mv(@CliOption(key = {"object"}, mandatory = true, help = "Source object key") final ObjectKey sourceObjectKey,
                     @CliOption(key = {""}, mandatory = true, help = "Dest object key") final String destFilePath) {
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
    @CliCommand(value = "set", help = "Set object metadata")
    public String set(@CliOption(key = {"key"}, mandatory = true, help = "Metadata key") final HttpHeader httpHeader,
                      @CliOption(key = {"value"}, mandatory = true, help = "Metadata value") final String value,
                      @CliOption(key = {""}, mandatory = true, help = "Object key") final String filePath) {
        try {
            String key = httpHeader.getName();
            aliyunOssService.setObjectMetadata(currentBucket.getChildObjectUri(filePath), key, value);
        } catch (Exception e) {
            log.error("set", e);
            return e.getMessage();
        }
        return file(new ObjectKey(filePath));
    }

    /**
     * set object meta data
     *
     * @return content
     */
    @CliCommand(value = "demo", help = "Set object metadata")
    public String demo(@CliOption(key = {"key"}, mandatory = true, help = "Metadata key") final HttpHeader httpHeader,
                       @CliOption(key = {""}, mandatory = true, help = "bucket name") final BucketEnum bucketEnum) {
        return null;
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
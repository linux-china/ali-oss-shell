package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

import java.util.List;
import java.util.Map;


/**
 * aliyun oss service implementation test case
 *
 * @author linux_china
 */
public class AliyunOssServiceImplTest {
    /**
     * aliyun oss service
     */
    private static AliyunOssServiceImpl aliyunOssService;
    /**
     * bucket name
     */
    private String bucketName = "example-bucket";

    private static final ConfigurableMimeFileTypeMap mimeTypes = new ConfigurableMimeFileTypeMap();


    /**
     * setup logic, please create .aliyunoss.cfg in user home directory and fill access info
     *
     * @throws Exception exception
     */
    @BeforeAll
    public static void setUp() throws Exception {
        aliyunOssService = new AliyunOssServiceImpl();
        ConfigServiceImpl configService = new ConfigServiceImpl();
        configService.init();
        aliyunOssService.setConfigService(configService);
        aliyunOssService.refreshToken();
    }

    /**
     * test to list buckets
     *
     * @throws Exception exception
     */
    @Test
    public void testListBuckets() throws Exception {
        List<Bucket> buckets = aliyunOssService.getBuckets();
        for (Bucket bucket : buckets) {
            System.out.println(bucket.getName());
        }
    }

    /**
     * test to list bucket
     *
     * @throws Exception exception
     */
    @Test
    public void testList() throws Exception {
        long start = System.currentTimeMillis();
        ObjectListing objectListing = aliyunOssService.list(bucketName, "", 1000);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            objectSummary.setBucketName(bucketName);
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    /**
     * test to get object metadata
     *
     * @throws Exception exception
     */
    @Test
    public void testGetObjectMetadata() throws Exception {
        ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(new OSSUri(bucketName, "demo.jpg"));
        printMetaData(objectMetadata);
    }

    /**
     * print meta data information
     *
     * @param objectMetadata object metadata
     */
    private void printMetaData(ObjectMetadata objectMetadata) {
        System.out.println("---raw metadata-----");
        for (Map.Entry<String, Object> entry : objectMetadata.getRawMetadata().entrySet()) {
            System.out.println(StringUtils.rightPad(entry.getKey(), 20, ' ') + " : " + entry.getValue());
        }
        System.out.println("---user metadata-----");
        for (Map.Entry<String, String> entry : objectMetadata.getUserMetadata().entrySet()) {
            System.out.println(StringUtils.rightPad(entry.getKey(), 20, ' ') + " : " + entry.getValue());
        }
    }

    /**
     * test to put object
     *
     * @throws Exception exception
     */
    @Test
    public void testPutObject() throws Exception {
        String destFile = "demo.jpg";
        String sourceFile = "/tmp/demo.jpg";
        aliyunOssService.put(sourceFile, new OSSUri(bucketName, destFile));
        ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(new OSSUri(bucketName, destFile));
        printMetaData(objectMetadata);
    }
}

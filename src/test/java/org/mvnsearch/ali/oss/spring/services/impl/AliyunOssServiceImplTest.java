package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.*;
import junit.framework.TestCase;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.springframework.shell.support.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * aliyun oss service implementation test case
 *
 * @author linux_china
 */
public class AliyunOssServiceImplTest extends TestCase {
    /**
     * aliyun oss service
     */
    private AliyunOssServiceImpl aliyunOssService;
    /**
     * bucket name
     */
    private String bucketName = "faxianla_temp";

    /**
     * setup logic, please create .aliyunoss.cfg in user home directory and fill access info
     *
     * @throws Exception exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
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
    public void testList() throws Exception {
        long start = System.currentTimeMillis();
        ObjectListing objectListing = aliyunOssService.list(bucketName, "", 1000);
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            //System.out.println(objectSummary.getKey());
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    /**
     * test to get object metadata
     *
     * @throws Exception exception
     */
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
            System.out.println(StringUtils.padRight(entry.getKey(), 20, ' ') + " : " + entry.getValue());
        }
        System.out.println("---user metadata-----");
        for (Map.Entry<String, String> entry : objectMetadata.getUserMetadata().entrySet()) {
            System.out.println(StringUtils.padRight(entry.getKey(), 20, ' ') + " : " + entry.getValue());
        }
    }

    /**
     * test to put object
     *
     * @throws Exception exception
     */
    public void testPutObject() throws Exception {
        String destFile = "demo.jpg";
        String sourceFile = "/Users/linux_china/demo.jpg";
        aliyunOssService.put(sourceFile, new OSSUri(bucketName, destFile));
        ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(new OSSUri(bucketName, destFile));
        printMetaData(objectMetadata);
    }
}

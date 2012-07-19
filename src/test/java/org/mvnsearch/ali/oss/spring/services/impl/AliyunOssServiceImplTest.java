package org.mvnsearch.ali.oss.spring.services.impl;

import com.aliyun.openservices.oss.model.Bucket;
import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import junit.framework.TestCase;
import org.springframework.shell.support.util.StringUtils;

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
        ObjectListing objectListing = aliyunOssService.list(bucketName, "");
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
        }
    }

    /**
     * test to get object metadata
     *
     * @throws Exception exception
     */
    public void testGetObjectMetadata() throws Exception {
        ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(bucketName, "demo.jpg");
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
        aliyunOssService.put(bucketName, sourceFile, destFile);
        ObjectMetadata objectMetadata = aliyunOssService.getObjectMetadata(bucketName, destFile);
        printMetaData(objectMetadata);
    }
}

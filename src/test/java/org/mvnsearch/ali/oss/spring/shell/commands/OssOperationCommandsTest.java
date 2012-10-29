package org.mvnsearch.ali.oss.spring.shell.commands;

import junit.framework.TestCase;
import org.mvnsearch.ali.oss.spring.services.BucketAclType;
import org.mvnsearch.ali.oss.spring.services.impl.AliyunOssServiceImpl;
import org.mvnsearch.ali.oss.spring.shell.converters.BucketEnum;

/**
 * oss operation commands test
 *
 * @author linux_china
 */
public class OssOperationCommandsTest extends TestCase {
    /**
     * oss operation commands
     */
    private OssOperationCommands commands;

    /**
     * init object
     *
     * @throws Exception exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.commands = new OssOperationCommands();
        this.commands.setAliyunOssService(new AliyunOssServiceImpl());
    }

    /**
     * test to list buckets
     */
    public void testDf() {
        BucketEnum bucketEnum = new BucketEnum();
        bucketEnum.setName("linux_china");
        commands.use(bucketEnum);
        System.out.println(commands.df());
    }

    /**
     * test to create bucket
     */
    public void testCreate() {
        String bucketName = "abc123";
        System.out.println(commands.create(BucketAclType.Private, bucketName));
        assertTrue("Failed to create bucket", commands.df().contains(bucketName));
    }
}

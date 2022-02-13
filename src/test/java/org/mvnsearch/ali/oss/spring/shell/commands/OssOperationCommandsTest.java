package org.mvnsearch.ali.oss.spring.shell.commands;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mvnsearch.ali.oss.spring.services.BucketAclType;
import org.mvnsearch.ali.oss.spring.services.impl.AliyunOssServiceImpl;
import org.mvnsearch.ali.oss.spring.services.impl.ConfigServiceImpl;
import org.mvnsearch.ali.oss.spring.shell.converters.BucketEnum;

/**
 * oss operation commands test
 *
 * @author linux_china
 */
public class OssOperationCommandsTest {
    /**
     * oss operation commands
     */
    private static OssOperationCommands commands;

    /**
     * init object
     *
     * @throws Exception exception
     */
    @BeforeAll
    public static void setUp() throws Exception {
        final ConfigServiceImpl configService = new ConfigServiceImpl();
        configService.init();
        final AliyunOssServiceImpl aliyunOssService = new AliyunOssServiceImpl();
        aliyunOssService.setConfigService(configService);
        aliyunOssService.refreshToken();
        commands = new OssOperationCommands();
        commands.setAliyunOssService(aliyunOssService);
        commands.init();
    }

    /**
     * test to list buckets
     */
    @Test
    public void testDf() {
        BucketEnum bucketEnum = new BucketEnum();
        bucketEnum.setName("davao-page");
        commands.use(bucketEnum);
        System.out.println(commands.df());
    }

    /**
     * test to create bucket
     */
    @Test
    public void testCreate() {
        String bucketName = "abc123";
        System.out.println(commands.create(BucketAclType.Private, bucketName));
    }
}

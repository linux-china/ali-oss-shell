package org.mvnsearch.ali.oss.spring.shell.commands;

import junit.framework.TestCase;
import org.mvnsearch.ali.oss.spring.services.impl.AliyunOssServiceImpl;

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
        commands.use("faxianla_temp");
        System.out.println(commands.df());
    }
}

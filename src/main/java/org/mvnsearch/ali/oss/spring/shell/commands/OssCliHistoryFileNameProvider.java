package org.mvnsearch.ali.oss.spring.shell.commands;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.stereotype.Component;

/**
 * aliyun oss history file name provider
 *
 * @author linux_china
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OssCliHistoryFileNameProvider extends DefaultHistoryFileNameProvider {

    /**
     * history log file name
     *
     * @return log file name
     */
    public String getHistoryFileName() {
        return ".oss-history.log";
    }

    /**
     * file name provider
     *
     * @return file name provider
     */
    @Override
    public String name() {
        return "aliyun-oss-java-cli-history-filename-provider";
    }

}

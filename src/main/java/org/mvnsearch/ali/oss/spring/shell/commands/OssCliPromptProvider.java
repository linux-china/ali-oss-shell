package org.mvnsearch.ali.oss.spring.shell.commands;

import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * aliyun OSS prompt provider
 *
 * @author linux_china
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OssCliPromptProvider extends DefaultPromptProvider implements InitializingBean {
    /**
     * prompt
     */
    public static String prompt = "AliOSS";
    /**
     * symbol
     */
    public static String symbol = "#";

    /**
     * config service
     */
    private ConfigService configService;

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
     * init method
     *
     * @throws Exception exception
     */
    public void afterPropertiesSet() throws Exception {
        String currentBucket = configService.getProperty("BUCKET");
        if (currentBucket != null) {
            prompt = "oss://" + currentBucket;
        }
        //if Windows OS, adjust symbo to '>'
        if (File.separator.equals("\\")) {
            symbol = ">";
        }
    }

    /**
     * prompt
     *
     * @return prompt
     */
    @Override
    public String getPrompt() {
        return "[" + prompt + "]" + symbol;
    }

    /**
     * name
     *
     * @return name
     */
    @Override
    public String name() {
        return "aliyun-oss-java-cli-prompt-provider";
    }

}

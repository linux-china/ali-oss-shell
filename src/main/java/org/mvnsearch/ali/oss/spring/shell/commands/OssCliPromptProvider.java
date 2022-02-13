package org.mvnsearch.ali.oss.spring.shell.commands;

import org.apache.commons.lang3.SystemUtils;
import org.jline.utils.AttributedString;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * aliyun OSS prompt provider
 *
 * @author linux_china
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OssCliPromptProvider implements PromptProvider, InitializingBean {
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
    public void afterPropertiesSet() {
        String currentBucket = configService.getProperty("BUCKET");
        if (currentBucket != null) {
            prompt = "oss://" + currentBucket;
        }
        //if Windows OS, adjust symbol to '>'
        if ((SystemUtils.IS_OS_WINDOWS)) {
            symbol = ">";
        }
    }

    /**
     * prompt
     *
     * @return prompt
     */
    @Override
    public AttributedString getPrompt() {
        return new AttributedString("[" + prompt + "]" + symbol);
    }

}

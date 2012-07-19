package org.mvnsearch.ali.oss.spring.shell.commands;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

/**
 * aliyun OSS prompt provider
 *
 * @author linux_china
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OssCliPromptProvider extends DefaultPromptProvider {
    /**
     * prompt
     *
     * @return prompt
     */
    @Override
    public String getPrompt() {
        return "Aliyun-OSS>";
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

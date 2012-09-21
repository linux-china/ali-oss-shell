package org.mvnsearch.ali.oss.spring.shell.commands;

import org.apache.commons.lang3.SystemUtils;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.stereotype.Component;

/**
 * aliyun OSS CLI banner provider
 *
 * @author linux_china
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OssCliBannerProvider extends DefaultBannerProvider implements CommandMarker {
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
     * get CLI banner
     *
     * @return banner text
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public String getBanner() {
        StringBuilder buf = new StringBuilder();
        buf.append("=======================================" + SystemUtils.LINE_SEPARATOR);
        buf.append("*                                     *" + SystemUtils.LINE_SEPARATOR);
        buf.append("*      Aliyun OSS Console             *" + SystemUtils.LINE_SEPARATOR);
        buf.append("*                                     *" + SystemUtils.LINE_SEPARATOR);
        buf.append("=======================================");
        return buf.toString();
    }

    /**
     * display author information
     *
     * @return author information
     */
    @CliCommand(value = {"author"}, help = "Displays author information")
    public String author() {
        return "Jacky Chan <linux_china@hotmail.com>, Please follow me: http://weibo.com/linux2china";
    }

    /**
     * current version
     *
     * @return version
     */
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * welcome message
     *
     * @return welcome message
     */
    public String getWelcomeMessage() {
        if (configService.available()) {
            return "Welcome to Aliyun OSS Console! Version: "+getVersion();
        } else {
            return "Please use config command to set access info!";
        }
    }

    /**
     * commander name
     *
     * @return name
     */
    @Override
    public String name() {
        return "aliyun-oss-java-cli";
    }
}
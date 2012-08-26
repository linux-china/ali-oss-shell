package org.mvnsearch.ali.oss.spring.shell.commands;

import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.support.util.StringUtils;
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
    @CliCommand(value = {"version"}, help = "Displays current CLI version")
    public String getBanner() {
        StringBuilder buf = new StringBuilder();
        buf.append("=======================================" + StringUtils.LINE_SEPARATOR);
        buf.append("*                                     *" + StringUtils.LINE_SEPARATOR);
        buf.append("*      Aliyun OSS Console             *" + StringUtils.LINE_SEPARATOR);
        buf.append("*                                     *" + StringUtils.LINE_SEPARATOR);
        buf.append("=======================================" + StringUtils.LINE_SEPARATOR);
        buf.append("Version:" + this.getVersion());
        return buf.toString();
    }

    /**
     * display author information
     *
     * @return author information
     */
    @CliCommand(value = {"author"}, help = "Displays author information")
    public String author() {
        return "linux_china <linux_china@hotmail.com>";
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
            return "Welcome to Aliyun OSS Java CLI";
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
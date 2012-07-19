package org.mvnsearch.ali.oss.spring.shell.commands;

import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
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
     * aliyun oss service
     */
    private AliyunOssService aliyunOssService;

    /**
     * inject aliyun oss service
     *
     * @param aliyunOssService aliyun oss service
     */
    @Autowired
    public void setAliyunOssService(AliyunOssService aliyunOssService) {
        this.aliyunOssService = aliyunOssService;
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
        buf.append("*      Aliyun OSS Java CLI            *" + StringUtils.LINE_SEPARATOR);
        buf.append("*                                     *" + StringUtils.LINE_SEPARATOR);
        buf.append("=======================================" + StringUtils.LINE_SEPARATOR);
        buf.append("Version:" + this.getVersion());
        return buf.toString();
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
        if (aliyunOssService.available()) {
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
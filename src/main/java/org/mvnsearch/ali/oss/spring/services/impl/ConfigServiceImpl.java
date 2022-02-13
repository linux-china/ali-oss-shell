package org.mvnsearch.ali.oss.spring.services.impl;

import org.jetbrains.annotations.Nullable;
import org.mvnsearch.ali.oss.spring.services.ConfigService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * config service implementation
 *
 * @author linux_china
 */
@Component("configService")
public class ConfigServiceImpl implements ConfigService {
    /**
     * configuration file name
     */
    private final String cfgFileName = ".aliyunoss.cfg";
    /**
     * global properties
     */
    private Properties properties;

    /**
     * post construct
     */
    @PostConstruct
    public void init() {
        try {
            properties = new Properties();
            File cfgFile = new File(new File(System.getProperty("user.home")), cfgFileName);
            if (cfgFile.exists()) {
                properties.load(new FileInputStream(cfgFile));
            }
        } catch (Exception ignore) {

        }
    }

    /**
     * available info
     *
     * @return available info
     */
    @Override
    public boolean available() {
        return properties.containsKey("ACCESS_ID") && properties.containsKey("ACCESS_KEY");
    }

    /**
     * get configuration
     *
     * @param key key
     * @return value
     */
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * update config
     *
     * @param key   key
     * @param value value
     */
    @Override
    public void setProperty(String key, @Nullable String value) {
        try {
            File cfgFile = new File(new File(System.getProperty("user.home")), cfgFileName);
            if (value == null) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value);
            }
            properties.store(new FileOutputStream(cfgFile), null);
        } catch (Exception ignore) {

        }
    }

    /**
     * set access info
     *
     * @param accessId  access id
     * @param accessKey access key
     */
    public void setAccessInfo(String accessId, String accessKey) {
        try {
            properties.setProperty("ACCESS_ID", accessId);
            properties.setProperty("ACCESS_KEY", accessKey);
            File cfgFile = new File(new File(System.getProperty("user.home")), cfgFileName);
            properties.store(new FileOutputStream(cfgFile), null);
        } catch (Exception ignore) {

        }
    }

    /**
     * set local file repository
     *
     * @param repository repository
     */
    public void setRepository(String repository) {
        setProperty("REPOSITORY", repository);
    }

    /**
     * get local repository
     *
     * @return local repository
     */
    @Override
    public String getRepository() {
        return getProperty("REPOSITORY");
    }
}

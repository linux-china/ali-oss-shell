package org.mvnsearch.ali.oss.spring.services;

/**
 * config service
 *
 * @author linux_china
 */
public interface ConfigService {

    /**
     * available info
     *
     * @return available infor
     */
    public boolean available();

    /**
     * set access info
     *
     * @param accessId  access id
     * @param accessKey access key
     */
    public void setAccessInfo(String accessId, String accessKey);

    /**
     * get configuration
     *
     * @param key key
     * @return value
     */
    String getProperty(String key);

    /**
     * update config
     *
     * @param key   key
     * @param value value
     */
    void setProperty(String key, String value);
}

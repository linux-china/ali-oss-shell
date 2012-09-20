package org.mvnsearch.ali.oss.spring.services;

/**
 * search manager
 *
 * @author linux_china
 */
public interface SearchManager {
    /**
     * index document
     *
     * @param document document
     */
    public void index(OssObjectDocument document);

    /**
     * find by object key with path
     *
     * @param objectKey object key
     * @return document
     */
    public OssObjectDocument findByObjectKey(String objectKey);

}

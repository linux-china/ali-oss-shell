package org.mvnsearch.ali.oss.spring.services.impl;

import org.mvnsearch.ali.oss.spring.services.OssObjectDocument;
import org.mvnsearch.ali.oss.spring.services.SearchManager;

/**
 * search manager implementation
 *
 * @author linux_china
 */
public class SearchManagerImpl implements SearchManager {
    /**
     * index document
     *
     * @param document document
     */
    public void index(OssObjectDocument document) {

    }

    /**
     * find by object key with path
     *
     * @param objectKey object key
     * @return document
     */
    public OssObjectDocument findByObjectKey(String objectKey) {
        return null;
    }
}

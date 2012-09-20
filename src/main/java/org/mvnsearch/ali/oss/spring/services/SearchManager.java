package org.mvnsearch.ali.oss.spring.services;

import java.util.List;

/**
 * search manager
 *
 * @author linux_china
 */
public interface SearchManager {
    /**
      * index document
      *
      * @param objectDocuments document
      */
     public void index(List<OssObjectDocument> objectDocuments);

    /**
     * find by object key with path
     *
     * @param objectKey object key
     * @return document
     */
    public OssObjectDocument findByObjectKey(String objectKey);

}

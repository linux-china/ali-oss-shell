package org.mvnsearch.ali.oss.spring.services;

import com.aliyun.openservices.oss.model.OSSObjectSummary;

/**
 * object listing iterator
 *
 * @author linux_china
 */
public class ObjectListingIterator {
    /**
     * count
     */
    int count = 0;

    /**
     * has next object
     *
     * @return next mark
     */
    public boolean hasNext() {
        return true;
    }

    /**
     * next oss object summary
     *
     * @return object summary
     */
    public OSSObjectSummary next() {
        return null;
    }

    /**
     * total count
     *
     * @return total count
     */
    public int totalCount() {
        return count;
    }
}

package org.mvnsearch.ali.oss.spring.shell.converters;


import com.aliyun.oss.model.Bucket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * bucket enum
 *
 * @author linux_china
 */
public class BucketEnum {
    /**
     * bucket name
     */
    private String name;
    /**
     * bucket name list
     */
    private static final Set<String> bucketNames = new HashSet<String>();

    /**
     * get bucket names
     *
     * @return bucket names
     */
    public static Set<String> getBucketNames() {
        return bucketNames;
    }

    /**
     * add bucket name
     *
     * @param name bucket name
     */
    public static void addBucketName(String name) {
        bucketNames.add(name);
    }

    /**
     * clear
     */
    public static void clear() {
        bucketNames.clear();
    }

    /**
     * set bucket name
     *
     * @param name bucket name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get bucket name
     *
     * @return bucket name
     */
    public String getName() {
        return this.name;
    }

    /**
     * string text
     *
     * @return name
     */
    public String toString() {
        return name;
    }

    /**
     * reset bucket name
     *
     * @param buckets oss bucket list
     */
    public static void reset(List<Bucket> buckets) {
        if (buckets != null) {
            clear();
            for (Bucket bucket : buckets) {
                addBucketName(bucket.getName());
            }
        }
    }

}

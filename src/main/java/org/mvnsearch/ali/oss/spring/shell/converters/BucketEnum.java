package org.mvnsearch.ali.oss.spring.shell.converters;

import java.util.*;

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
    private static Set<String> bucketNames = new HashSet<String>();

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
     * set name
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * string text
     *
     * @return name
     */
    public String toString() {
        return name;
    }

}

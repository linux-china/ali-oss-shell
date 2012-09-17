package org.mvnsearch.ali.oss.spring.shell.converters;

import java.util.Arrays;
import java.util.List;

/**
 * http header
 *
 * @author linux_china
 */
public class HttpHeader {
    /**
     * name
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<String> getDefaultNames() {
        return Arrays.asList("Cache-Control", "Content-Type", "Expires");
    }
}

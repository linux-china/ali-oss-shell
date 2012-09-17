package org.mvnsearch.ali.oss.spring.services;

/**
 * bucket acl type
 *
 * @author linux_china
 */
public enum BucketAclType {

    Private("Private"),
    ReadOnly("ReadOnly"),
    ReadWrite("ReadWrite");

    private String type;

    private BucketAclType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * short code
     *
     * @return short code
     */
    public String getShortCode() {
        if (type.equalsIgnoreCase("ReadWrite")) {
            return "RW";
        } else if (type.equalsIgnoreCase("ReadOnly")) {
            return "R-";
        } else {
            return "Private";
        }
    }
}

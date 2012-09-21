package org.mvnsearch.ali.oss.spring.services.impl;

import junit.framework.TestCase;
import org.mvnsearch.ali.oss.spring.services.OssObjectDocument;
import org.mvnsearch.ali.oss.spring.services.SearchManager;

import java.util.Arrays;
import java.util.Date;

/**
 * search manager implementation test
 *
 * @author linux_china
 */
public class SearchManagerImplTest extends TestCase {
    /**
     * search manager
     */
    private SearchManager searchManager;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        searchManager = new SearchManagerImpl();
    }

    public void testIndex() throws Exception {
        OssObjectDocument document = new OssObjectDocument();
        document.setBucket("linux_china");
        document.setPath("cms");
        document.setName("demo.png");
        document.setContentType("image/png");
        document.setContentLength(130000);
        document.setDate(new Date());
        searchManager.index(Arrays.asList(document));
    }
}

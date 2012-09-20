package org.mvnsearch.ali.oss.spring.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.mvnsearch.ali.oss.spring.services.OssObjectDocument;
import org.mvnsearch.ali.oss.spring.services.SearchManager;

import java.io.File;
import java.util.List;

/**
 * search manager implementation
 *
 * @author linux_china
 */
public class SearchManagerImpl implements SearchManager {
    /**
     * repository directory
     */
    File repositoryDirectory = new File(new File(System.getProperty("user.home")), "aliyun_oss/.lucene");

    /**
     * index document
     *
     * @param objectDocuments document
     */
    public void index(List<OssObjectDocument> objectDocuments) {
        try {
            IndexWriter indexWriter = new IndexWriter(repositoryDirectory, new StandardAnalyzer(), false, IndexWriter.MaxFieldLength.UNLIMITED);
            for (OssObjectDocument objectDocument : objectDocuments) {
                Document doc = new Document();
                doc.add(new Field("bucket", objectDocument.getBucket(), Field.Store.NO, Field.Index.NOT_ANALYZED));
                if (StringUtils.isNotEmpty(objectDocument.getPath())) {
                    doc.add(new Field("path", objectDocument.getPath(), Field.Store.NO, Field.Index.ANALYZED));
                }
                doc.add(new Field("name", objectDocument.getName(), Field.Store.NO, Field.Index.ANALYZED));
                doc.add(new Field("contentType", objectDocument.getContentType(), Field.Store.NO, Field.Index.NOT_ANALYZED));
                doc.add(new Field("contentLength", String.valueOf(objectDocument.getContentLength()), Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("objectUri", objectDocument.getObjectUri(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                indexWriter.addDocument(doc);
            }
            indexWriter.commit();
            indexWriter.close();
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
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

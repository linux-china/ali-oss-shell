package org.mvnsearch.ali.oss.spring.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * zip utils
 *
 * @author linux_china
 */
public class ZipUtils {
    /**
     * buffer size
     */
    public static int BUFFER = 1024;

    /**
     * compress
     *
     * @param plainContent plain content
     * @return compressed content
     * @throws Exception exception
     */
    public static byte[] compress(byte[] plainContent) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        gos.write(plainContent);
        gos.finish();
        gos.flush();
        gos.close();
        return bos.toByteArray();
    }

    /**
     * uncompress content
     *
     * @param zipContent zip content
     * @return plain content
     * @throws Exception exception
     */
    public static byte[] uncompress(byte[] zipContent) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(zipContent));
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = gis.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, count);
        }
        gis.close();
        return bos.toByteArray();
    }
}

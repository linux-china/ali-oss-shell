package org.mvnsearch.ali.oss.spring.shell.converters;

import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectListing;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.mvnsearch.ali.oss.spring.shell.commands.OssOperationCommands;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;

/**
 * object key converter
 *
 * @author linux_china
 */
public class ObjectKeyConverter implements Converter<ObjectKey> {
    /**
     * aliyun OSS service
     */
    private AliyunOssService aliyunOssService;

    /**
     * inject aliyun oss service
     *
     * @param aliyunOssService aliyun oss service
     */
    public void setAliyunOssService(AliyunOssService aliyunOssService) {
        this.aliyunOssService = aliyunOssService;
    }

    /**
     * Indicates whether this converter supports the given type in the given option context
     *
     * @param type          the type being checked
     * @param optionContext a non-<code>null</code> string that customises the
     *                      behaviour of this converter for a given {@link org.springframework.shell.core.annotation.CliOption} of a given
     *                      {@link org.springframework.shell.core.annotation.CliCommand}; the contents will have special meaning to this
     *                      converter (e.g. be a comma-separated list of keywords known to this
     *                      converter)
     * @return see above
     */
    public boolean supports(Class<?> type, String optionContext) {
        return type.getCanonicalName().equals(ObjectKey.class.getCanonicalName());
    }

    /**
     * Converts from the given String value to type T
     *
     * @param value         the value to convert
     * @param targetType    the type being converted to; can't be <code>null</code>
     * @param optionContext a non-<code>null</code> string that customises the
     *                      behaviour of this converter for a given {@link org.springframework.shell.core.annotation.CliOption} of a given
     *                      {@link org.springframework.shell.core.annotation.CliCommand}; the contents will have special meaning to this
     *                      converter (e.g. be a comma-separated list of keywords known to this
     *                      converter)
     * @return see above
     * @throws RuntimeException if the given value could not be converted
     */
    @SuppressWarnings("unchecked")
    public ObjectKey convertFromText(String value, Class<?> targetType, String optionContext) {
        Class<ObjectKey> enumClass = (Class<ObjectKey>) targetType;
        try {
            ObjectKey bucket = enumClass.newInstance();
            bucket.setKey(value);
            return bucket;
        } catch (Exception ignore) {

        }
        return null;
    }

    /**
     * Populates the given list with the possible completions
     *
     * @param completions   the list to populate; can't be <code>null</code>
     * @param targetType    the type of parameter for which a string is being entered
     * @param existingData  what the user has typed so far
     * @param optionContext a non-<code>null</code> string that customises the
     *                      behaviour of this converter for a given {@link org.springframework.shell.core.annotation.CliOption} of a given
     *                      {@link org.springframework.shell.core.annotation.CliCommand}; the contents will have special meaning to this
     *                      converter (e.g. be a comma-separated list of keywords known to this
     *                      converter)
     * @param target target method
     * @return <code>true</code> if all the added completions are complete
     *         values, or <code>false</code> if the user can press TAB to add further
     *         information to some or all of them
     */
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        OSSUri objectUri = OssOperationCommands.currentBucket;
        if (existingData != null && existingData.startsWith("oss://")) {
            return true;
        }
        try {
            String bucketName = objectUri.getBucket();
            String key = objectUri.getFilePath();
            ObjectListing objectListing = aliyunOssService.list(bucketName, key + existingData, 40);
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                String candidate = objectSummary.getKey();
                if (key != null && candidate.startsWith(key)) {
                    candidate = candidate.replace(key, "");
                }
                if (existingData == null || "".equals(existingData) || candidate.startsWith(existingData) || existingData.startsWith(candidate) || candidate.toUpperCase().startsWith(existingData.toUpperCase()) || existingData.toUpperCase().startsWith(candidate.toUpperCase())) {
                    completions.add(new Completion(candidate));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}

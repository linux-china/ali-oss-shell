package org.mvnsearch.ali.oss.spring.shell.converters;

import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.jetbrains.annotations.NotNull;
import org.mvnsearch.ali.oss.spring.services.AliyunOssService;
import org.mvnsearch.ali.oss.spring.services.OSSUri;
import org.mvnsearch.ali.oss.spring.shell.commands.OssOperationCommands;
import org.springframework.core.convert.converter.Converter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * object key converter
 *
 * @author linux_china
 */
@Component
public class ObjectKeyConverter implements Converter<String, ObjectKey>, ValueProvider {
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

    @Override
    public ObjectKey convert(@NotNull String source) {
        return new ObjectKey(source);
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String existingData = completionContext.currentWord();
        OSSUri objectUri = OssOperationCommands.currentBucket;
        if (existingData != null && existingData.startsWith("oss://")) {
            return Collections.emptyList();
        }
        try {
            String bucketName = objectUri.getBucket();
            String key = objectUri.getFilePath();
            ObjectListing objectListing = aliyunOssService.list(bucketName, key + existingData, 40);
            List<CompletionProposal> completions = new ArrayList<>();
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                String candidate = objectSummary.getKey();
                if (key != null && candidate.startsWith(key)) {
                    candidate = candidate.replace(key, "");
                }
                if (existingData == null || "".equals(existingData) || candidate.startsWith(existingData) || existingData.startsWith(candidate) || candidate.toUpperCase().startsWith(existingData.toUpperCase()) || existingData.toUpperCase().startsWith(candidate.toUpperCase())) {
                    completions.add(new CompletionProposal(candidate));
                }
            }
            return completions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}

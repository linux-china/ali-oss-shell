package org.mvnsearch.ali.oss.spring.shell.converters;


import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * bucket enum converter
 *
 * @author linux_china
 */
@Component
public class BucketEnumConverter implements Converter<String, BucketEnum>, ValueProvider {


    @Override
    public BucketEnum convert(@NotNull String value) {
        BucketEnum bucket = new BucketEnum();
        bucket.setName(value);
        return bucket;

    }

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        return BucketEnum.getBucketNames().stream().map(CompletionProposal::new).toList();
    }
}

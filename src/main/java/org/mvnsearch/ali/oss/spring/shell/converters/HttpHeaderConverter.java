package org.mvnsearch.ali.oss.spring.shell.converters;


import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * http header converter
 *
 * @author linux_china
 */
@Component
public class HttpHeaderConverter implements Converter<String, HttpHeader>, ValueProvider {

    @Override
    public HttpHeader convert(@NotNull String value) {
        HttpHeader bucket = new HttpHeader();
        bucket.setName(value);
        return bucket;
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        return HttpHeader.getDefaultNames().stream().map(CompletionProposal::new).toList();
    }
}

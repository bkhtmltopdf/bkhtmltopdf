package com.bkhtmltopdf.renderer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;

public class DefaultRendererOptionsProvider implements RendererOptionsProvider {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public RendererOptions parse(JsonNode json) {
        final RendererOptions options = new RendererOptions();

        final JsonNode pdf = json.get("pdf");
        if (pdf != null && pdf.isObject()) {
            options.setPdf(objectMapper.treeToValue(pdf, RendererOptions.PDF.class));
        }

        final JsonNode opts = json.get("options");
        if (opts != null && opts.isObject()) {
            options.setOptions(objectMapper.treeToValue(opts, RendererOptions.Options.class));
        }

        final JsonNode markdown = json.get("markdown");
        if (markdown != null && markdown.isObject()) {
            options.setMarkdown(objectMapper.treeToValue(markdown, RendererOptions.Markdown.class));
        }

        options.setId((Optional.ofNullable(json.get("id")))
                .map(JsonNode::asString)
                .orElseGet(() -> UUID.randomUUID().toString()));

        return options;
    }

    @Configuration
    static class RendererOptionsProviderConfig {
        @Bean
        @ConditionalOnMissingBean(RendererOptionsProvider.class)
        public RendererOptionsProvider rendererOptionsProvider() {
            return new DefaultRendererOptionsProvider();
        }
    }
}

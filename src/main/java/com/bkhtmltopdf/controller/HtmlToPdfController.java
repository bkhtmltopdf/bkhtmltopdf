package com.bkhtmltopdf.controller;

import com.bkhtmltopdf.renderer.RendererOptions;
import com.bkhtmltopdf.renderer.RendererOptionsProvider;
import com.bkhtmltopdf.service.HtmlToPdfService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RestController
@ConditionalOnBooleanProperty("bkhtmltopdf.controller.enabled")
class HtmlToPdfController extends BaseController {


    @Resource
    private HtmlToPdfService htmlToPdfService;
    @Resource
    private RendererOptionsProvider rendererOptionsProvider;


    @PostMapping(value = "/html-to-pdf")
    Object html2pdf(@RequestBody JsonNode json) {
        final RendererOptions options = rendererOptionsProvider.parse(json);
        final var html = Optional.ofNullable(json.get("html")).map(JsonNode::asText)
                .orElse(StringUtils.EMPTY);
        return response(htmlToPdfService.print(html, options));
    }

    protected ResponseEntity<FileSystemResource> response(File pdf) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                .body(new FileSystemResource(pdf) {
                    @NonNull
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new BufferedInputStream(super.getInputStream()) {
                            @Override
                            public void close() throws IOException {
                                try {
                                    super.close();
                                } finally {
                                    FileUtils.deleteQuietly(pdf);
                                }
                            }
                        };
                    }
                });
    }

}

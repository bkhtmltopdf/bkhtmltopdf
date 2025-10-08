package com.bkhtmltopdf.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BaseController {
    @Resource
    protected HttpServletRequest request;
    @Resource
    protected HttpServletResponse response;

    protected ResponseEntity<FileSystemResource> responseFile(File pdf,MediaType mediaType) {
        return ResponseEntity.ok().contentType(mediaType)
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

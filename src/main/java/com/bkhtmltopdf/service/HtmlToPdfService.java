package com.bkhtmltopdf.service;

import com.bkhtmltopdf.renderer.RendererOptions;

import java.io.File;

public interface HtmlToPdfService {
    File print(String html, RendererOptions options);
}

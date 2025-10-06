package com.bkhtmltopdf.renderer;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.jspecify.annotations.NonNull;

import java.io.File;

public interface HtmlRenderer {
    @NonNull
    CefBrowser render(@NonNull CefClient cefClient, @NonNull File html, @NonNull RendererOptions options);
}

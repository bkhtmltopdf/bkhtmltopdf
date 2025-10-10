package com.bkhtmltopdf.renderer;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.jspecify.annotations.NonNull;

public interface HtmlRenderer {
    @NonNull
    CefBrowser render(@NonNull CefClient cefClient, @NonNull String html, @NonNull RendererOptions options);
}

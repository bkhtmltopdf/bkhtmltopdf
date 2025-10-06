package com.bkhtmltopdf.cef;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnusedReturnValue")
public interface CefHandler {

    @NonNull
    CefClient createCefClient();

    void disposeCefClient(@NonNull CefClient cefClient);

    @NonNull
    CefBrowser createCefBrowser(@NonNull CefClient cefClient, @NonNull String url);

    void disposeCefBrowser(@NonNull CefBrowser cefBrowser);
}

package com.bkhtmltopdf.service;

import com.bkhtmltopdf.cef.CefHandler;
import com.bkhtmltopdf.config.BkHtmlToPdfConfig;
import com.bkhtmltopdf.renderer.HtmlRenderer;
import com.bkhtmltopdf.renderer.RendererOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefCookieAccessFilter;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.BoolRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.cef.network.CefURLRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public abstract class HtmlToAnyService {
    @Resource
    private BkHtmlToPdfConfig config;
    @Resource
    private TempService tempService;
    @Resource
    private CefHandler cefHandler;
    @Resource
    private HtmlRenderer htmlRenderer;

    protected File print(String html, RendererOptions options) {
        final File file = tempService.createTempFile("html");
        try (var fis = new FileOutputStream(file)) {
            IOUtils.write(html, fis, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        try {
            final CefClient cefClient = createCefClient();
            try {
                final CefBrowser browser = htmlRenderer.render(cefClient, file, options);
                try {
                    return to(browser, options);
                } finally {
                    cefHandler.disposeCefBrowser(browser);
                }
            } finally {
                cefHandler.disposeCefClient(cefClient);
            }
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    protected File to(CefBrowser browser, RendererOptions options) {
        throw new UnsupportedOperationException();
    }

    protected CefClient createCefClient() {
        final CefClient cefClient = cefHandler.createCefClient();
        cefClient.addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
                final String url = request.getURL();
                if (StringUtils.startsWithIgnoreCase(url, "file")) {
                    try {
                        final URI uri = URI.create(url);
                        final String path = FilenameUtils.normalize(uri.getPath());
                        if (path.startsWith(tempService.getTemporaryFile())) {
                            return super.getResourceRequestHandler(browser, frame, request, isNavigation, isDownload, requestInitiator, disableDefaultHandling);
                        }
                    } catch (Throwable e) {
                        if (log.isErrorEnabled()) {
                            log.error("Invalid URL: {}", url, e);
                        }
                    }
                } else if (StringUtils.startsWithIgnoreCase(url, "http:") || StringUtils.startsWithIgnoreCase(url, "https:")) {
                    return super.getResourceRequestHandler(browser, frame, request, isNavigation, isDownload, requestInitiator, disableDefaultHandling);
                }
                return new HtmlToPdfServiceImpl.BlockCefResourceRequestHandler();
            }
        });
        return cefClient;
    }


    protected Duration getTimeout() {
        return config.getRenderer().getTimeout();
    }


    protected static class BlockCefResourceRequestHandler implements CefResourceRequestHandler {

        @Override
        public CefCookieAccessFilter getCookieAccessFilter(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest) {
            return null;
        }

        @Override
        public boolean onBeforeResourceLoad(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest) {
            return true;
        }

        @Override
        public CefResourceHandler getResourceHandler(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest) {
            return null;
        }

        @Override
        public void onResourceRedirect(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, CefResponse cefResponse, StringRef stringRef) {
        }

        @Override
        public boolean onResourceResponse(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, CefResponse cefResponse) {
            return false;
        }

        @Override
        public void onResourceLoadComplete(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, CefResponse cefResponse, CefURLRequest.Status status, long l) {
        }

        @Override
        public void onProtocolExecution(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, BoolRef boolRef) {
        }
    }


}

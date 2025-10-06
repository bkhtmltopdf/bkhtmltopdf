package com.bkhtmltopdf.renderer;

import com.bkhtmltopdf.cef.CefHandler;
import com.bkhtmltopdf.config.BkHtmlToPdfConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DefaultHtmlRenderer implements HtmlRenderer {


    @Resource
    private CefHandler cefHandler;
    @Resource
    private BkHtmlToPdfConfig config;


    @NonNull
    @Override
    public CefBrowser render(@NonNull CefClient cefClient, @NonNull File html, @NonNull RendererOptions options) {
        return createCefBrowser(cefClient, html.toURI().toString(), options);
    }

    protected CefBrowser createCefBrowser(CefClient cefClient, String url, RendererOptions options) {

        final var future = registerPrintHandler(cefClient, url, options);

        cefHandler.createCefBrowser(cefClient, url);

        try {
            return future.get(getTimeout(options).toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException("Due to an internal system error, the HTML failed to load", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Failed to load HTML, possibly due to timeout when loading resources such as img tags", e);
        }
    }

    protected Future<CefBrowser> registerPrintHandler(CefClient cefClient, String url, RendererOptions options) {
        final var future = new CompletableFuture<CefBrowser>();
        final String printFlag = "print: " + options.getId();
        final String waitUntil = options.getOptions().getWaitUntil();

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {

                if (level == CefSettings.LogSeverity.LOGSEVERITY_ERROR) {
                    log.error(message);
                } else if (level == CefSettings.LogSeverity.LOGSEVERITY_WARNING) {
                    log.warn(message);
                } else if (level == CefSettings.LogSeverity.LOGSEVERITY_INFO) {
                    log.info(message);
                } else if (level == CefSettings.LogSeverity.LOGSEVERITY_VERBOSE) {
                    log.trace(message);
                }

                if (printFlag.equals(message) || ("manual".equals(waitUntil) && "print".equals(message))) {
                    if (future.isCancelled() || future.isDone() || future.isCompletedExceptionally()) {
                        return false;
                    }
                    future.complete(browser);
                }

                return false;
            }
        });

        cefClient.addLoadHandler(new CefLoadHandlerAdapter() {
            private final AtomicBoolean isInjected = new AtomicBoolean(false);

            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
                if (frame.isMain()) {
                    if (StringUtils.equals(waitUntil, "domcontentloaded")) {
                        if (isInjected.compareAndSet(false, true)) {
                            browser.executeJavaScript("""
                                    const message = '%s'
                                    if (document.readyState === 'loading') {
                                        window.addEventListener('DOMContentLoaded', () => {
                                            console.debug(message)
                                        });
                                    } else {
                                        console.debug(message)
                                    }
                                    """.formatted(printFlag), browser.getURL(), 0);
                        }
                    }
                }
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
                if (future.isCancelled() || future.isDone() || future.isCompletedExceptionally()) {
                    return;
                }

                if (frame.isMain()) {
                    future.completeExceptionally(new IllegalStateException(errorText));
                }
            }


            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                if (future.isCancelled() || future.isDone() || future.isCompletedExceptionally()) {
                    return;
                }

                if (frame.isMain()) {
                    if (StringUtils.equals(waitUntil, "load") || StringUtils.isBlank(waitUntil)) {
                        future.complete(browser);
                    }
                }
            }

        });


        return future;
    }

    protected Duration getTimeout(RendererOptions options) {
        if (options.getOptions().getTimeout() == null) {
            return config.getRenderer().getTimeout();
        }
        return Duration.ofMillis(options.getOptions().getTimeout());
    }


    @Configuration
    static class DefaultHtmlRendererConfig {
        @Bean
        @ConditionalOnMissingBean(HtmlRenderer.class)
        public HtmlRenderer htmlRenderer() {
            return new DefaultHtmlRenderer();
        }
    }
}

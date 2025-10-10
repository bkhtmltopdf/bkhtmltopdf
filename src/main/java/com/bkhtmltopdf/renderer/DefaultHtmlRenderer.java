package com.bkhtmltopdf.renderer;

import com.bkhtmltopdf.cef.CefHandler;
import com.bkhtmltopdf.config.BkHtmlToPdfConfig;
import com.bkhtmltopdf.service.TempService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class DefaultHtmlRenderer implements HtmlRenderer {


    @Resource
    private CefHandler cefHandler;
    @Resource
    private BkHtmlToPdfConfig config;
    @Resource
    private TempService tempService;


    @NonNull
    @Override
    public CefBrowser render(@NonNull CefClient cefClient, @NonNull String html, @NonNull RendererOptions options) {
        final File file = tempService.createTempFile("html");
        try (var fos = new BufferedOutputStream(new FileOutputStream(file))) {
            injectPrintCode(fos, options);
            IOUtils.write(html, fos, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return createCefBrowser(cefClient, file.toURI().toString(), options);
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
        final Consumer<Object> callback = new Consumer<>() {
            @Override
            public synchronized void accept(Object object) {
                if (future.isCancelled() || future.isDone() || future.isCompletedExceptionally()) {
                    return;
                }
                if (object instanceof CefBrowser browser) {
                    future.complete(browser);
                } else if (object instanceof Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            }
        };

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            private final String printFlag = "print: " + options.getId();

            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {

                if (config.getConsole().isEnabled()) {
                    if (level == CefSettings.LogSeverity.LOGSEVERITY_ERROR) {
                        log.error(message);
                    } else if (level == CefSettings.LogSeverity.LOGSEVERITY_WARNING) {
                        log.warn(message);
                    } else if (level == CefSettings.LogSeverity.LOGSEVERITY_INFO) {
                        log.info(message);
                    } else if (level == CefSettings.LogSeverity.LOGSEVERITY_VERBOSE) {
                        log.trace(message);
                    }
                }

                if (printFlag.equals(message)) {
                    callback.accept(browser);
                }

                return false;
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

    protected void injectPrintCode(OutputStream os, RendererOptions options) throws IOException {
        final var waitUntil = options.getOptions().getWaitUntil();
        final StringBuilder sb = new StringBuilder("<script>");

        sb.append("""
                Object.defineProperty(window, 'print', {
                    value: function() {
                        console.log(`%s`)
                    },
                    writable: false,
                    configurable: false
                });
                """.formatted("print: " + options.getId()));

        if (waitUntil == null || waitUntil == RendererOptions.WaitUntil.load) {
            sb.append("window.addEventListener('load', window.print);");
        } else if (waitUntil == RendererOptions.WaitUntil.domcontentloaded) {
            sb.append("""
                    if (document.readyState === 'loading') {
                        window.addEventListener('DOMContentLoaded', window.print);
                    } else {
                        window.print();
                    }
                    """);
        }

        sb.append("</script>");

        os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
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

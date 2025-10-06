package com.bkhtmltopdf.cef;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.apache.commons.io.FileUtils;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Slf4j
class JCefHandler implements InitializingBean, DisposableBean, CefHandler {
    @Resource
    private Environment environment;

    private CefApp cefApp;

    @Override
    public void afterPropertiesSet() throws Exception {
        final var builder = new CefAppBuilder();
        final var file = new File("jcef");
        final File cache = new File(file, "cache");
        FileUtils.forceMkdir(file);
        FileUtils.forceMkdir(cache);

        builder.setInstallDir(new File(file, "bundle"));
        builder.setProgressHandler(new ConsoleProgressHandler());
        builder.addJcefArgs("--disable-gpu",
                "--disable-notifications",
                "--disable-features=NativeNotifications",
                "--disable-component-update",
                "--disable-software-rasterizer",
                "--disable-features=Vulkan",
                "--disable-extensions",
                "--disable-web-security",
                "--user-data-dir=" + new File(file, "data").getAbsolutePath());
        builder.getCefSettings().windowless_rendering_enabled = false;
        builder.getCefSettings().root_cache_path = cache.getAbsolutePath();
        builder.getCefSettings().log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;
        builder.getCefSettings().log_file = new File(file, "jcef.log").getAbsolutePath();
        final String version = environment.getProperty("spring.application.version");
        builder.getCefSettings().user_agent = "bkhtmltopdf CE " + version + " (AGPL version)";
        builder.setMirrors(List.of(
                "https://mirrors.huaweicloud.com/repository/maven/me/friwi/jcef-natives-{platform}/{tag}/jcef-natives-{platform}-{tag}.jar",
                "https://repo.maven.apache.org/maven2/me/friwi/jcef-natives-{platform}/{tag}/jcef-natives-{platform}-{tag}.jar"));

        cefApp = builder.build();

    }

    @Override
    @NonNull
    public synchronized CefClient createCefClient() {
        return cefApp.createClient();
    }

    @Override
    public synchronized void disposeCefClient(@NonNull CefClient cefClient) {
        cefClient.dispose();
    }

    @Override
    @NonNull
    public CefBrowser createCefBrowser(@NonNull CefClient cefClient, @NonNull String url) {
        final CefBrowser browser = cefClient.createBrowser(url, false, true);
        browser.createImmediately();
        return browser;
    }

    @Override
    public void disposeCefBrowser(@NonNull CefBrowser cefBrowser) {
        cefBrowser.close(true);
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(cefApp)) {
            cefApp.dispose();
        }
    }


    @Configuration
    static class CefConfig {

        @Bean
        @ConditionalOnMissingBean(CefHandler.class)
        public CefHandler cefHandler() {
            return new JCefHandler();
        }
    }

}

package com.bkhtmltopdf.service;

import com.bkhtmltopdf.renderer.RendererOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.cef.browser.CefBrowser;
import org.cef.misc.CefPdfPrintSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HtmlToPdfServiceImpl extends HtmlToAnyService implements HtmlToPdfService {

    @Resource
    private TempService tempService;

    @Override
    public File print(String html, RendererOptions options) {
        return super.print(html, options);
    }

    @Override
    protected File to(CefBrowser browser, RendererOptions options) {
        return printToPdf(browser, options);
    }

    protected File printToPdf(CefBrowser cefBrowser, RendererOptions options) {
        final var future = new CompletableFuture<Boolean>();

        final File pdf = tempService.createTempFile();

        cefBrowser.printToPDF(pdf.getAbsolutePath(), getCefPdfPrintSettings(options), (path, ok) -> future.complete(ok));

        try {
            if (future.get(getTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                return pdf;
            } else {
                throw new IllegalStateException("The print to PDF failed due to an unknown error");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Due to an internal system error, the PDF cannot be printed", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Printing to PDF has failed. This may be due to insufficient system resources or other issues", e);
        }
    }

    protected CefPdfPrintSettings getCefPdfPrintSettings(RendererOptions options) {
        final var settings = new CefPdfPrintSettings();
        settings.prefer_css_page_size = true;
        settings.margin_type = CefPdfPrintSettings.MarginType.NONE;
        settings.scale = 1.0;
        settings.print_background = true;

        final RendererOptions.PDF pdf = options.getPdf();
        settings.footer_template = StringUtils.defaultString(pdf.getFooterTemplate());
        settings.header_template = StringUtils.defaultString(pdf.getHeaderTemplate());
        settings.display_header_footer = BooleanUtils.toBoolean(pdf.getDisplayHeaderFooter());

        settings.generate_tagged_pdf = BooleanUtils.toBoolean(pdf.getGenerateTaggedPdf());
        settings.generate_document_outline = BooleanUtils.toBoolean(pdf.getGenerateDocumentOutline());


        return settings;
    }


    @Configuration
    static class HtmlToPdfServiceConfig {
        @Bean
        @ConditionalOnMissingBean(HtmlToPdfService.class)
        HtmlToPdfService htmlToPdfService() {
            return new HtmlToPdfServiceImpl();
        }
    }

}

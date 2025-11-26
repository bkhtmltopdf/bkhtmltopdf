package com.bkhtmltopdf.controller;

import com.bkhtmltopdf.renderer.RendererOptions;
import com.bkhtmltopdf.renderer.RendererOptionsProvider;
import com.bkhtmltopdf.service.HtmlToPdfService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.html.HtmlGeneratorKt;
import org.intellij.markdown.parser.MarkdownParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

@RestController
@ConditionalOnBooleanProperty("bkhtmltopdf.controller.md-to-pdf.enabled")
class MarkdownToPdfController extends BaseController {


    @Resource
    private HtmlToPdfService htmlToPdfService;
    @Resource
    private RendererOptionsProvider rendererOptionsProvider;


    @PostMapping(value = "/md-to-pdf")
    Object html2pdf(@RequestBody JsonNode json) {
        final RendererOptions options = rendererOptionsProvider.parse(json);
        final var markdown = Optional.ofNullable(json.get("text")).map(JsonNode::asString)
                .orElse(StringUtils.EMPTY);

        MarkdownFlavourDescriptor flavour;

        if (StringUtils.isBlank(options.getMarkdown().getFlavour()) || Strings.CI.equals("COMMON", options.getMarkdown().getFlavour())) {
            flavour = new CommonMarkFlavourDescriptor();
        } else if (Strings.CI.equals("GFM", options.getMarkdown().getFlavour())) {
            flavour = new GFMFlavourDescriptor();
        } else if (Strings.CI.equals("SFM", options.getMarkdown().getFlavour())) {
            flavour = new SFMFlavourDescriptor();
        } else {
            throw new IllegalArgumentException("Invalid markdown flavour:" + options.getMarkdown().getFlavour());
        }

        final var tree = new MarkdownParser(flavour).buildMarkdownTreeFromString(markdown);

        final String html = new HtmlGenerator(markdown, tree, flavour, false)
                .generateHtml(new HtmlGenerator.DefaultTagRenderer(HtmlGeneratorKt.getDUMMY_ATTRIBUTES_CUSTOMIZER(), false));

        return responseFile(htmlToPdfService.print(html, options), MediaType.APPLICATION_PDF);
    }


}

package com.bkhtmltopdf.controller;

import com.bkhtmltopdf.renderer.RendererOptions;
import com.bkhtmltopdf.renderer.RendererOptionsProvider;
import com.bkhtmltopdf.service.HtmlToPdfService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@ConditionalOnBooleanProperty("bkhtmltopdf.controller.enabled")
class HtmlToPdfController extends BaseController {


    @Resource
    private HtmlToPdfService htmlToPdfService;
    @Resource
    private RendererOptionsProvider rendererOptionsProvider;


    @PostMapping(value = "/html-to-pdf")
    Object html2pdf(@RequestBody JsonNode json) {
        final RendererOptions options = rendererOptionsProvider.parse(json);
        final var html = Optional.ofNullable(json.get("text")).map(JsonNode::asText)
                .orElse(StringUtils.EMPTY);
        return responseFile(htmlToPdfService.print(html, options), MediaType.APPLICATION_PDF);
    }


}

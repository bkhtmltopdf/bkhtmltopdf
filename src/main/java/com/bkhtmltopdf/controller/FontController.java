package com.bkhtmltopdf.controller;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@ConditionalOnBooleanProperty("bkhtmltopdf.controller.enabled")
class FontController extends BaseController {

    @Getter
    @Setter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class FontInfo {
        @EqualsAndHashCode.Include
        private String family;
    }

    @GetMapping(value = "/fonts")
    Object fonts() {
        final Set<FontInfo> fonts = new LinkedHashSet<>();
        for (Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            final FontInfo fontInfo = new FontInfo();
            fontInfo.family = font.getFamily();
            fonts.add(fontInfo);
        }
        return fonts;
    }


}

package com.bkhtmltopdf.renderer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@Getter
@Setter
public class RendererOptions {

    private String id = UUID.randomUUID().toString();

    @NonNull
    private PDF pdf = new PDF();

    @NonNull
    private Options options = new Options();

    @Getter
    @Setter
    public static class PDF {
        @JsonProperty("footerTemplate")
        private String footerTemplate;
        @JsonProperty("headerTemplate")
        private String headerTemplate;
        @JsonProperty("displayHeaderFooter")
        private Boolean displayHeaderFooter;
        @JsonProperty("generateTaggedPdf")
        private Boolean generateTaggedPdf;
        @JsonProperty("generateDocumentOutline")
        private Boolean generateDocumentOutline;
    }

    @Getter
    @Setter
    public static class Options {
        /**
         * load | domcontentloaded | manual
         */
        @JsonProperty("waitUntil")
        private String waitUntil;


        /**
         * timeout, milliseconds, default is 15000
         */
        @JsonProperty("timeout")
        private Long timeout;
    }

}

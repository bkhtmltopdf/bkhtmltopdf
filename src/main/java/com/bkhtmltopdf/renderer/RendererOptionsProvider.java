package com.bkhtmltopdf.renderer;

import com.fasterxml.jackson.databind.JsonNode;

public interface RendererOptionsProvider {
    RendererOptions parse(JsonNode json);
}

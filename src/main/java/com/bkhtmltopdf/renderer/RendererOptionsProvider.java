package com.bkhtmltopdf.renderer;

import tools.jackson.databind.JsonNode;

public interface RendererOptionsProvider {
    RendererOptions parse(JsonNode json);
}

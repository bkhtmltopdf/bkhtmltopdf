package com.bkhtmltopdf.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("bkhtmltopdf")
public class BkHtmlToPdfConfig {

    private final Controller controller = new Controller();
    private final Renderer renderer = new Renderer();
    private final Console console = new Console();


    @Getter
    @Setter
    public static class Renderer {
        private Duration timeout = Duration.ofSeconds(15);
    }

    @Getter
    @Setter
    public static class Controller {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Console {
        private boolean enabled = false;
    }
}

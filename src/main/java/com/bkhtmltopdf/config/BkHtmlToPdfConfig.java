package com.bkhtmltopdf.config;

import com.google.errorprone.annotations.Keep;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Keep
@Getter
@Setter
@Configuration
@ConfigurationProperties("bkhtmltopdf")
public class BkHtmlToPdfConfig {

    private final Controller controller = new Controller();
    private final Renderer renderer = new Renderer();
    private final Console console = new Console();


    @Keep
    @Getter
    @Setter
    public static class Renderer {
        private Duration timeout = Duration.ofSeconds(15);
    }


    @Keep
    @Getter
    @Setter
    public static class Controller {
        private final Enabled htmlToPdf = new Enabled();
        private final Enabled mdToPdf = new Enabled();
        private final Enabled pdfToJpg = new Enabled();
        private final Enabled fonts = new Enabled();
    }


    @Keep
    @Getter
    @Setter
    public static class Console {
        private boolean enabled = false;
    }

    @Keep
    @Getter
    @Setter
    public static class Enabled {
        private boolean enabled = true;
    }
}

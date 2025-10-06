package com.bkhtmltopdf;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .headless(Boolean.getBoolean("java.awt.headless"))
                .main(Application.class)
                .sources(Application.class)
                .run(args);
    }
}

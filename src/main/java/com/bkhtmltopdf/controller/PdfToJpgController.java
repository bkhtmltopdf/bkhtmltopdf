package com.bkhtmltopdf.controller;

import com.bkhtmltopdf.service.TempService;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@ConditionalOnBooleanProperty("bkhtmltopdf.controller.pdf-to-jpg.enabled")
class PdfToJpgController extends BaseController {

    @Resource
    private TempService tempService;

    @Getter
    @Setter
    static class Options {
        @JsonProperty("scale")
        private Float scale = 1.0f;

        @Nullable
        @JsonProperty("password")
        private String password = null;
    }


    @SneakyThrows
    @PostMapping(value = "/pdf-to-jpg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Object pdf2jpg(@RequestParam MultipartFile file, Options options) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file cannot be null or empty");
        }

        final File tempFile = tempService.createTempFile("pdf");
        final float scale = Math.min(Math.max(ObjectUtils.defaultIfNull(options.getScale(), 1.0f), 0.1f), 5.0f);


        try (final InputStream is = file.getInputStream(); final FileOutputStream fos = new FileOutputStream(tempFile)) {
            IOUtils.copy(is, fos);

            try (PDDocument document = Loader.loadPDF(tempFile, StringUtils.defaultString(options.getPassword()))) {
                final int numberOfPages = document.getNumberOfPages();
                final PDFRenderer renderer = new PDFRenderer(document);
                renderer.setRenderingHints(new RenderingHints(Map.of(
                        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                        RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                ));

                final File zip = tempService.createTempFile("zip");
                try {
                    String format = "page_%0" + String.valueOf(numberOfPages).length() + "d.jpg";
                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
                        for (int i = 0; i < numberOfPages; i++) {
                            zos.putNextEntry(new ZipEntry(format.formatted(i + 1)));
                            ImageIO.write(renderer.renderImage(i, scale, ImageType.RGB), "jpg", zos);
                            zos.closeEntry();
                        }
                    }
                } catch (Exception e) {
                    FileUtils.deleteQuietly(zip);
                    throw e;
                }

                return responseFile(zip, MediaType.APPLICATION_OCTET_STREAM, ContentDisposition.attachment()
                        .filename(URLEncoder.encode(FilenameUtils.getBaseName(file.getOriginalFilename()) + ".zip", StandardCharsets.UTF_8))
                        .build().toString());
            }
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }

    }


}

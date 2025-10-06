package com.bkhtmltopdf.service;

import org.jspecify.annotations.NonNull;

import java.io.File;

public interface TempService {
    /**
     * @param suffix txt、pdf、html
     */
    @NonNull
    File createTempFile(@NonNull String suffix);

    @NonNull
    File createTempFile();

    @NonNull
    File createTempDirectory();

    @NonNull
    String getTemporaryFile();
}

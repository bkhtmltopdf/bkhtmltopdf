package com.bkhtmltopdf.service;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
class TempServiceImpl implements TempService, InitializingBean, DisposableBean {

    private final File dir = new File(FileUtils.getTempDirectory(), "bkhtmltopdf");


    @Override
    @NonNull
    public File createTempFile(@NonNull String suffix) {
        return new File(dir, UUID.randomUUID() + "." + suffix);
    }

    @Override
    @NonNull
    public File createTempFile() {
        return new File(dir, UUID.randomUUID().toString());
    }

    @NonNull
    @Override
    @SneakyThrows
    public File createTempDirectory() {
        final File file = new File(dir, UUID.randomUUID().toString());
        FileUtils.forceMkdir(file);
        return file;
    }

    @Override
    @NonNull
    public String getTemporaryFile() {
        val path = StringUtils.removeEnd(dir.getAbsolutePath(), File.separator);
        return path + File.separator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        FileUtils.forceMkdir(dir);
        FileUtils.cleanDirectory(dir);
    }

    @Override
    public void destroy() throws Exception {
        FileUtils.cleanDirectory(dir);
    }
}

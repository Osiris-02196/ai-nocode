package com.oxiris.yuaicodemother.service;

import com.oxiris.yuaicodemother.model.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     *
     * @param projectPath
     * @param downloadFileName
     * @param response
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}

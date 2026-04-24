package com.oxiris.yuaicodemother.service;

public interface ScreenshotService {

    /**
     * 通用的截图服务，可以得到访问地址
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}

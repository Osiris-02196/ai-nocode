package com.oxiris.yuaicodemother.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.oxiris.yuaicodemother.langgraph4j.model.ImageResource;
import com.oxiris.yuaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UndrawIllustrationTool {

    private static final String UNDRAW_BASE_URL = "https://undraw.co";

    private volatile String cachedBuildId = "";

    /**
     * 动态获取 Next.js build ID（每次部署都会变化，不能硬编码）
     */
    /**
     *   1. 先请求 https://undraw.co 首页
     *   2. 从 HTML 中的 __NEXT_DATA__ 脚本标签解析出最新的 buildId
     *   3. 缓存到内存中（volatile + synchronized 双重检查），后续请求不再重复获取
     *   4. 如果 __NEXT_DATA__ 提取失败，还有兜底策略：从 _buildManifest.js 路径中提取
     * @return
     */
    private String getBuildId() {
        if (StrUtil.isNotBlank(cachedBuildId)) {
            return cachedBuildId;
        }
        synchronized (this) {
            if (StrUtil.isNotBlank(cachedBuildId)) {
                return cachedBuildId;
            }
            try (HttpResponse response = HttpRequest.get(UNDRAW_BASE_URL).timeout(8000).execute()) {
                if (!response.isOk()) {
                    return null;
                }
                String body = response.body();
                // 从 __NEXT_DATA__ 中提取 buildId
                String scriptTag = StrUtil.subBetween(body,
                        "<script id=\"__NEXT_DATA__\" type=\"application/json\">", "</script>");
                if (scriptTag != null) {
                    JSONObject nextData = JSONUtil.parseObj(scriptTag);
                    cachedBuildId = nextData.getStr("buildId");
                    log.info("动态获取到 undraw.co buildId: {}", cachedBuildId);
                    return cachedBuildId;
                }
                // 兜底：从 _buildManifest.js 路径提取
                String manifestPart = StrUtil.subBetween(body, "/_next/static/", "/_buildManifest.js");
                if (manifestPart != null) {
                    cachedBuildId = manifestPart;
                    log.info("从 manifest 获取到 buildId: {}", cachedBuildId);
                    return cachedBuildId;
                }
            } catch (Exception e) {
                log.error("获取 undraw.co buildId 失败", e);
            }
            return null;
        }
    }

    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12;

        String buildId = getBuildId();
        if (buildId == null) {
            log.warn("无法获取 undraw.co buildId，搜索插画失败");
            return imageList;
        }

        String apiUrl = String.format("%s/_next/data/%s/search/%s.json?term=%s",
                UNDRAW_BASE_URL, buildId, query, query);

        try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {
            if (!response.isOk()) {
                return imageList;
            }
            JSONObject result = JSONUtil.parseObj(response.body());
            JSONObject pageProps = result.getJSONObject("pageProps");
            if (pageProps == null) {
                return imageList;
            }
            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null || initialResults.isEmpty()) {
                return imageList;
            }
            int actualCount = Math.min(searchCount, initialResults.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("media", "");
                if (StrUtil.isNotBlank(media)) {
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.ILLUSTRATION)
                            .description(title)
                            .url(media)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }
}

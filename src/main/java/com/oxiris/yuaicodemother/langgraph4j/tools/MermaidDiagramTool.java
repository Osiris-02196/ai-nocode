package com.oxiris.yuaicodemother.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.oxiris.yuaicodemother.exception.BusinessException;
import com.oxiris.yuaicodemother.exception.ErrorCode;
import com.oxiris.yuaicodemother.langgraph4j.model.ImageResource;
import com.oxiris.yuaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import com.oxiris.yuaicodemother.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * mermaid架构图生成工具
 */
@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;

    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 转换为SVG图片
            File diagramFile = convertMermaidToSvg(mermaidCode);
            // 上传到COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);
            // 清理临时文件
            FileUtil.del(diagramFile);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将Mermaid代码转换为SVG图片
     */
    private File convertMermaidToSvg(String mermaidCode) {
        // 创建临时输入文件
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, tempInputFile);
        // 创建临时输出文件
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
        File puppeteerConfig = null;
        try {
            // 构建命令参数
            List<String> cmdArgs = new ArrayList<>(Arrays.asList(resolveMmdcCommand()));
            cmdArgs.add("-i");
            cmdArgs.add(tempInputFile.getAbsolutePath());
            cmdArgs.add("-o");
            cmdArgs.add(tempOutputFile.getAbsolutePath());
            cmdArgs.add("-b");
            cmdArgs.add("transparent");
            // puppeteer 配置（指向系统 Chrome/Edge）
            puppeteerConfig = createPuppeteerConfig();
            cmdArgs.add("-p");
            cmdArgs.add(puppeteerConfig.getAbsolutePath());

            log.debug("执行命令: {}", String.join(" ", cmdArgs));
            ProcessBuilder pb = new ProcessBuilder(cmdArgs);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            log.debug("mmdc 退出码: {}, 输出: {}", exitCode, output);

            if (exitCode != 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "Mermaid CLI 执行失败, exitCode=" + exitCode + ", output=" + output);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行异常: " + e.getMessage());
        } finally {
            if (puppeteerConfig != null) {
                FileUtil.del(puppeteerConfig);
            }
        }
        // 检查输出文件
        if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败: 未生成输出文件");
        }
        // 清理输入文件，保留输出文件供上传使用
        FileUtil.del(tempInputFile);
        return tempOutputFile;
    }

    /**
     * 创建 puppeteer 临时配置文件，指向系统安装的 Chrome/Edge
     */
    private File createPuppeteerConfig() throws IOException {
        String browserPath = detectBrowser().replace("\\", "\\\\");
        String config = String.format("{\"executablePath\":\"%s\",\"args\":[\"--no-sandbox\"]}", browserPath);
        File configFile = FileUtil.createTempFile("puppeteer_config_", ".json", true);
        FileUtil.writeUtf8String(config, configFile);
        return configFile;
    }

    /**
     * 检测系统已安装的 Chrome 或 Edge 浏览器路径
     */
    private String detectBrowser() {
        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String[] candidates = {
                programFiles + "\\Google\\Chrome\\Application\\chrome.exe",
                programFilesX86 + "\\Google\\Chrome\\Application\\chrome.exe",
                programFilesX86 + "\\Microsoft\\Edge\\Application\\msedge.exe",
                programFiles + "\\Microsoft\\Edge\\Application\\msedge.exe",
                System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("LOCALAPPDATA") + "\\Microsoft\\Edge\\Application\\msedge.exe",
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                log.debug("检测到浏览器: {}", path);
                return path;
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未找到 Chrome 或 Edge 浏览器，请安装 Chrome/Edge");
    }

    /**
     * 解析 mmdc 命令，返回命令参数列表（优先用 node 直接执行，避免包装脚本的路径问题）
     */
    private String[] resolveMmdcCommand() {
        if (!SystemUtil.getOsInfo().isWindows()) {
            return new String[]{"mmdc"};
        }
        // 优先用 node 直接执行 mermaid-cli 入口文件，跳过损坏的包装脚本
        String homeDir = System.getProperty("user.home");
        String[] candidatePaths = {
                homeDir + "\\AppData\\Local\\Yarn\\Data\\global\\node_modules\\@mermaid-js\\mermaid-cli\\src\\cli.js",
                System.getenv("LOCALAPPDATA") + "\\Yarn\\Data\\global\\node_modules\\@mermaid-js\\mermaid-cli\\src\\cli.js",
        };
        for (String cliPath : candidatePaths) {
            if (new File(cliPath).exists()) {
                return new String[]{"node", cliPath};
            }
        }
        // 兜底：尝试 yarn global bin 目录下的 mmdc.cmd（可能包装脚本损坏）
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "yarn global bin"});
            String binDir = new String(process.getInputStream().readAllBytes()).trim();
            process.waitFor();
            if (StrUtil.isNotBlank(binDir)) {
                String fullPath = binDir + File.separator + "mmdc.cmd";
                if (new File(fullPath).exists()) {
                    return new String[]{fullPath};
                }
            }
        } catch (Exception ignored) {}
        log.warn("未能找到 mermaid-cli 入口文件，将尝试直接使用 mmdc 命令");
        return new String[]{"mmdc"};
    }
}

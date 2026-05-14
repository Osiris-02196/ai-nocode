package com.oxiris.yuaicodemother.langgraph4j.ai;

import com.oxiris.yuaicodemother.langgraph4j.tools.ImageSearchTool;
import com.oxiris.yuaicodemother.langgraph4j.tools.MermaidDiagramTool;
import com.oxiris.yuaicodemother.langgraph4j.tools.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片收集服务工厂
 */
@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    //使用流式的原因：deepseek会thinking，但是chatmodel处理不了thinking的内容
    // 改为流式就可以把 reasoning_content作为独立 SSE event 发送的
    @Resource
    private StreamingChatModel streamingChatModelPrototype;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;


    /**
     * 创建图片收集 AI 服务
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .streamingChatModel(streamingChatModelPrototype)
                .tools(
                        imageSearchTool,
                        undrawIllustrationTool,
                        mermaidDiagramTool
                )
                .build();
    }
}

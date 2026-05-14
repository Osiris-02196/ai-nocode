package com.oxiris.yuaicodemother.langgraph4j.node;

import com.oxiris.yuaicodemother.langgraph4j.ai.ImageCollectionService;
import com.oxiris.yuaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import com.oxiris.yuaicodemother.langgraph4j.model.ImageResource;
import com.oxiris.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.oxiris.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 * 使用AI进行工具调用，收集不同类型的图片
 */
@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                // 获取AI图片收集服务
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                // 使用 AI 服务进行智能图片收集
                imageListStr = imageCollectionService.collectImages(originalPrompt)
                        .collectList().block().stream().collect(Collectors.joining());

            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}


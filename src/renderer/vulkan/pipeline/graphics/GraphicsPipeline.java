package renderer.vulkan.pipeline.graphics;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import renderer.Renderer;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.pipeline.Pipeline;
import renderer.vulkan.renderPass.RenderPass;
import renderer.vulkan.vertexData.QuadVertexData;

public abstract class GraphicsPipeline extends Pipeline {    
    /*
     * constructors
     */
	public GraphicsPipeline() {
		super();
	}
	
	/*
	 * public methods
	 */    
    public void create(Renderer renderer, RenderPass renderPass) {
        try(MemoryStack stack = stackPush()) {
        	LogicalDevice logicalDevice = renderer.getLogicalDevice();
            LongBuffer pGraphicsPipeline = stack.mallocLong(1);
        	VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
        	
            VkPipelineShaderStageCreateInfo.Buffer stages = shaderStages.shaderStageCreateInfos(logicalDevice, stack);
            
            VkPipelineVertexInputStateCreateInfo vertexInputStateCreateInfo = vertexInputStateCreateInfo(stack);            
            VkPipelineInputAssemblyStateCreateInfo inputAssemblyStateCreateInfo = inputAssemblyStateCreateInfo(stack);
            VkPipelineViewportStateCreateInfo viewportStateCreateInfo = viewportStateCreateInfo(stack);
            VkPipelineRasterizationStateCreateInfo rasterizationStageCreateInfo = rasterizationStateCreateInfo(stack);
            VkPipelineMultisampleStateCreateInfo multisamplingStateCreateInfo = multisamplingStateCreateInfo(stack);
            VkPipelineDepthStencilStateCreateInfo depthStencil = depthStencilStateCreateInfo(stack);
            VkPipelineColorBlendStateCreateInfo colorBlendStateCreateInfo = colorBlendStateCreateInfo(stack);
            VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = dynamicStateCreateInfo(stack);
            
            createPipelineLayout(renderer);            
            
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(stages);
            pipelineInfo.pVertexInputState(vertexInputStateCreateInfo);
            pipelineInfo.pInputAssemblyState(inputAssemblyStateCreateInfo);
            pipelineInfo.pViewportState(viewportStateCreateInfo);
            pipelineInfo.pRasterizationState(rasterizationStageCreateInfo);
            pipelineInfo.pMultisampleState(multisamplingStateCreateInfo);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlendStateCreateInfo);
            pipelineInfo.layout(pipelineLayout.getPipelineLayout());
            pipelineInfo.renderPass(renderPass.renderPass());
            pipelineInfo.subpass(subpass());
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);
            pipelineInfo.pDynamicState(dynamicStateCreateInfo);

            if(VK_SUCCESS == vkCreateGraphicsPipelines(logicalDevice.device(), VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline)) {
                pipeline = pGraphicsPipeline.get(0);
            } else {
                throw new RuntimeException("Failed to create graphics pipeline");
            }

            shaderStages.destroy(logicalDevice);
        }
    }
    
    /*
     * protected methods
     */
    protected VkPipelineColorBlendStateCreateInfo colorBlendStateCreateInfo(MemoryStack stack) {
        VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
        colorBlendAttachment.blendEnable(true);
        colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
        colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
        colorBlendAttachment.colorBlendOp(VK_BLEND_OP_ADD);
        colorBlendAttachment.srcAlphaBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
        colorBlendAttachment.dstAlphaBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
        colorBlendAttachment.alphaBlendOp(VK_BLEND_OP_ADD);
        colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT |
        		VK_COLOR_COMPONENT_G_BIT |
        		VK_COLOR_COMPONENT_B_BIT |
        		VK_COLOR_COMPONENT_A_BIT);

        VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
        colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        colorBlending.logicOpEnable(false);
        colorBlending.logicOp(VK_LOGIC_OP_NO_OP);
        colorBlending.pAttachments(colorBlendAttachment);
        colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
        
        return colorBlending;
    }
    
    protected VkPipelineDepthStencilStateCreateInfo depthStencilStateCreateInfo(MemoryStack stack) {
        VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
        depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
        depthStencil.depthTestEnable(true);
        depthStencil.depthWriteEnable(true);
        depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
        depthStencil.depthBoundsTestEnable(false);
        depthStencil.minDepthBounds(0.0f);
        depthStencil.maxDepthBounds(1.0f);
        depthStencil.stencilTestEnable(false);
        depthStencil.back().compareOp(VK_COMPARE_OP_ALWAYS);
        
        return depthStencil;
    }
    
    protected VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo(MemoryStack stack) {
    	VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack);
    	IntBuffer pDynamicStates = stack.mallocInt(2);
    	
    	pDynamicStates.put(0, VK_DYNAMIC_STATE_VIEWPORT);
    	pDynamicStates.put(1, VK_DYNAMIC_STATE_SCISSOR);
    	
    	dynamicStateCreateInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
    	dynamicStateCreateInfo.pDynamicStates(pDynamicStates);
    	
    	return dynamicStateCreateInfo;
    }
    
    protected VkPipelineInputAssemblyStateCreateInfo inputAssemblyStateCreateInfo(MemoryStack stack) {
        VkPipelineInputAssemblyStateCreateInfo inputAssemblyStateCreateInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
        inputAssemblyStateCreateInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        inputAssemblyStateCreateInfo.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        inputAssemblyStateCreateInfo.primitiveRestartEnable(false);
        
        return inputAssemblyStateCreateInfo;
    }
    
    protected VkPipelineMultisampleStateCreateInfo multisamplingStateCreateInfo(MemoryStack stack) {
        VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
        multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        multisampling.sampleShadingEnable(false);
        multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
        
        return multisampling;
    }
    
    protected VkPipelineVertexInputStateCreateInfo vertexInputStateCreateInfo(MemoryStack stack) {
        VkPipelineVertexInputStateCreateInfo vertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
        vertexInputStateCreateInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vertexInputStateCreateInfo.pVertexBindingDescriptions(QuadVertexData.getBindingDescription());
        vertexInputStateCreateInfo.pVertexAttributeDescriptions(QuadVertexData.getAttributeDescriptions());
        
        return vertexInputStateCreateInfo;
    }
    
    protected VkPipelineRasterizationStateCreateInfo rasterizationStateCreateInfo(MemoryStack stack) {
        VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
        rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        rasterizer.depthClampEnable(false);
        rasterizer.rasterizerDiscardEnable(false);
        rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
        rasterizer.lineWidth(1.0f);
        rasterizer.cullMode(VK_CULL_MODE_NONE);
        rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
        rasterizer.depthBiasEnable(false);
        
        return rasterizer;
    }
    
    protected VkPipelineViewportStateCreateInfo viewportStateCreateInfo(MemoryStack stack) {
    	VkExtent2D imageExtent = VkExtent2D.calloc(stack);
    	
        VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
        viewport.x(0.0f);
        viewport.y(0.0f);
        viewport.width(imageExtent.width());
        viewport.height(imageExtent.height());
        viewport.minDepth(0.0f);
        viewport.maxDepth(1.0f);

        VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
        scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
        scissor.extent(imageExtent);

        VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
        viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        viewportState.pViewports(viewport);
        viewportState.pScissors(scissor);
        
        return viewportState;
    }
    
    protected int subpass() {return 0;}
}

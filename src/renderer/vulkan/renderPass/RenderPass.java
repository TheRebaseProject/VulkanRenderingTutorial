package renderer.vulkan.renderPass;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

import renderer.vulkan.LogicalDevice;

public class RenderPass {
    /*
     * protected variables
     */
	protected long renderPass;
	protected int subpassCount;
	
	/*
	 * constructors
	 */
	public RenderPass() {
		renderPass = VK_NULL_HANDLE;
		subpassCount = 1;
	}
	
	/*
	 * public methods
	 */
	public void beginRenderPass(int height, int width, long framebuffer, VkCommandBuffer commandBuffer) {
		try(MemoryStack stack = stackPush()) {
	    	VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack);
	        VkRect2D renderArea = VkRect2D.calloc(stack);
	        VkExtent2D extent = VkExtent2D.calloc();
	        VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
	        VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
	        
	        extent.height(height);
	        extent.width(width);
	        
	        renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
	        renderArea.extent(extent);
	    	
	        renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
	        renderPassBeginInfo.renderPass(renderPass);
	        renderPassBeginInfo.renderArea(renderArea);
	        renderPassBeginInfo.pClearValues(setClearValues(stack));
	        renderPassBeginInfo.framebuffer(framebuffer);
	        
	        viewport.x(0.0f);
	        viewport.y(0.0f);
	        viewport.width(extent.width());
	        viewport.height(extent.height());
	        viewport.minDepth(0.0f);
	        viewport.maxDepth(1.0f);
	        
	        scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
	        scissor.extent(extent);
	        
	        vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);            
	        vkCmdSetViewport(commandBuffer, 0, viewport);
	        vkCmdSetScissor(commandBuffer, 0, scissor);		
		}
	}
	
    public void create(LogicalDevice logicalDevice) {
        try(MemoryStack stack = stackPush()) {
            VkAttachmentDescription.Buffer attachments;
            VkAttachmentReference.Buffer attachmentRefs;
            VkSubpassDescription.Buffer subpassDescriptions;
            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            LongBuffer pRenderPass = stack.mallocLong(1);

            attachments = setAttachments(stack);
            attachmentRefs = setAttachmentRefs(stack);
            subpassDescriptions = setSubpassDescriptions(attachments, attachmentRefs, stack);

            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpassDescriptions);
            renderPassInfo.pDependencies(setDependencies(stack));

            if(VK_SUCCESS == vkCreateRenderPass(logicalDevice.device(), renderPassInfo, null, pRenderPass)) {
                renderPass = pRenderPass.get(0);
            } else {
                throw new RuntimeException("Failed to create a render pass");
            }
        }
    }
    
    public void destroy(LogicalDevice logicalDevice) {vkDestroyRenderPass(logicalDevice.device(), renderPass, null);}
    public int getSubpassCount() {return subpassCount;}
	public long renderPass() {return renderPass;}
	
	/*
	 * protected methods
	 */	
	protected VkAttachmentDescription.Buffer setAttachments(MemoryStack stack) {
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);
		
        attachments.format(VK_FORMAT_R16G16B16A16_SFLOAT);
        attachments.samples(VK_SAMPLE_COUNT_1_BIT);
        attachments.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        attachments.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        attachments.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        attachments.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        attachments.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        attachments.finalLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        
        return attachments;
	}
	
	protected VkAttachmentReference.Buffer setAttachmentRefs(MemoryStack stack) {
		VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(1, stack);
		
		attachmentRefs.attachment(0);
		attachmentRefs.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        
        return attachmentRefs;
	}
	
	protected VkClearValue.Buffer setClearValues(MemoryStack stack) {
		VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
		
		clearValues.color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
		
		return clearValues;
	}
	
	protected VkSubpassDependency.Buffer setDependencies(MemoryStack stack) {
		VkSubpassDependency.Buffer dependencies = VkSubpassDependency.calloc(2, stack);
		
		dependencies.get(0).srcSubpass(VK_SUBPASS_EXTERNAL);
		dependencies.get(0).dstSubpass(0);
		dependencies.get(0).srcStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT);
		dependencies.get(0).dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		dependencies.get(0).srcAccessMask(VK_ACCESS_MEMORY_READ_BIT);
		dependencies.get(0).dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
		dependencies.get(0).dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);

		dependencies.get(1).srcSubpass(0);
		dependencies.get(1).dstSubpass(VK_SUBPASS_EXTERNAL);
		dependencies.get(1).srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		dependencies.get(1).dstStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT);
		dependencies.get(1).srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
		dependencies.get(1).dstAccessMask(VK_ACCESS_MEMORY_READ_BIT);
		dependencies.get(1).dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);
		
		return dependencies;
	}
	
	protected VkSubpassDescription.Buffer setSubpassDescriptions(VkAttachmentDescription.Buffer attachments, VkAttachmentReference.Buffer attachmentRefs, MemoryStack stack) {
		VkSubpassDescription.Buffer subpassDescriptions = VkSubpassDescription.calloc(subpassCount, stack);
		
        subpassDescriptions.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpassDescriptions.colorAttachmentCount(attachments.capacity());
        subpassDescriptions.pColorAttachments(attachmentRefs);
		
		return subpassDescriptions;
	}
}

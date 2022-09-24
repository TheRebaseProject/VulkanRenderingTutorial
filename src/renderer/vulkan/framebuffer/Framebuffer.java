package renderer.vulkan.framebuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import renderer.vulkan.LogicalDevice;

public class Framebuffer {
    /*
     * private variables
     */
	private long framebuffer;
	
	/*
	 * constructors
	 */
	public Framebuffer() {
		framebuffer = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, long renderPass, int height, int width, long imageView) {
        try(MemoryStack stack = stackPush()) {
            LongBuffer attachments = stack.mallocLong(1);
            
            attachments.put(0, imageView);
            
            createFramebuffer(logicalDevice, renderPass, height, width, attachments);
        }
	}
	
	public void destroy(LogicalDevice logicalDevice) {vkDestroyFramebuffer(logicalDevice.device(), framebuffer, null);}
	public long framebuffer() {return framebuffer;}
	
	/*
	 * private methods
	 */
	private void createFramebuffer(LogicalDevice logicalDevice, long renderPass, int height, int width, LongBuffer attachments) {
		try(MemoryStack stack = stackPush()) {
            LongBuffer pFramebuffer = stack.mallocLong(1);
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(width);
            framebufferInfo.height(height);
            framebufferInfo.layers(1);
            framebufferInfo.pAttachments(attachments);

            if(VK_SUCCESS == vkCreateFramebuffer(logicalDevice.device(), framebufferInfo, null, pFramebuffer)) {
            	framebuffer = pFramebuffer.get(0);
            } else {
                throw new RuntimeException("Failed to create framebuffer");
            }
		}
	}
}

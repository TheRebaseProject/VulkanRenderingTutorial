package renderer.vulkan.renderPass;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;

import renderer.vulkan.LogicalDevice;

public class RenderPassPresentation extends RenderPass {
	/*
	 * private variables
	 */
	private int format;
	
	/*
	 * constructors
	 */
	public RenderPassPresentation() {
		super();
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, int format) {
		this.format = format;
		super.create(logicalDevice);
	}
	
	/*
	 * protected methods
	 */
	@Override
	protected VkAttachmentDescription.Buffer setAttachments(MemoryStack stack) {
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);
		
        attachments.format(format);
        attachments.samples(VK_SAMPLE_COUNT_1_BIT);
        attachments.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        attachments.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        attachments.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        attachments.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        attachments.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        attachments.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
        
        return attachments;
	}
}

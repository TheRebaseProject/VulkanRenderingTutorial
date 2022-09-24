package renderer.vulkan.image;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import renderer.vulkan.LogicalDevice;

public class ImageView {
    /*
     * private variables
     */
	private long imageView;
	
	/*
	 * constructors
	 */
	public ImageView() {
		imageView = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, long image, int viewType, int mipLevels, int format, int imageAspect, int layerCount) {
        try(MemoryStack stack = stackPush()) {
            VkImageViewCreateInfo viewCreateInfo = VkImageViewCreateInfo.calloc(stack);
            LongBuffer pImageView = stack.mallocLong(1);
            
            viewCreateInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewCreateInfo.image(image);
            viewCreateInfo.viewType(viewType);
            viewCreateInfo.format(format);
            
            viewCreateInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY);
            viewCreateInfo.components().g(VK_COMPONENT_SWIZZLE_IDENTITY);
            viewCreateInfo.components().b(VK_COMPONENT_SWIZZLE_IDENTITY);
            viewCreateInfo.components().a(VK_COMPONENT_SWIZZLE_IDENTITY);
            
            viewCreateInfo.subresourceRange().aspectMask(imageAspect);
            viewCreateInfo.subresourceRange().baseMipLevel(0);
            viewCreateInfo.subresourceRange().levelCount(mipLevels);
            viewCreateInfo.subresourceRange().baseArrayLayer(0);
            viewCreateInfo.subresourceRange().layerCount(layerCount);

            if(VK_SUCCESS == vkCreateImageView(logicalDevice.device(), viewCreateInfo, null, pImageView)) {
                imageView =  pImageView.get(0);
            } else {
                throw new RuntimeException("Failed to create texture image view");
            }
        }
	}
	
	public void destroy(LogicalDevice logicalDevice) {vkDestroyImageView(logicalDevice.device(), imageView, null);}
	public long getImageView() {return imageView;}
}

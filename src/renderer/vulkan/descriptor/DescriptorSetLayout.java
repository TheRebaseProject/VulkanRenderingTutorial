package renderer.vulkan.descriptor;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import renderer.vulkan.LogicalDevice;

public abstract class DescriptorSetLayout {
    /*
     * protected variables
     */
	protected long descriptorSetLayout;
	
	/*
	 * constructors
	 */
	public DescriptorSetLayout() {
		descriptorSetLayout = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
	public abstract void create(LogicalDevice logicalDevice);	
	public void destroy(LogicalDevice logicalDevice) {vkDestroyDescriptorSetLayout(logicalDevice.device(), descriptorSetLayout, null);}
	public long getDescriptorSetLayout() {return descriptorSetLayout;}
	
	/*
	 * protected methods
	 */
	protected void create(LogicalDevice logicalDevice, VkDescriptorSetLayoutBinding.Buffer bindings) {
		try(MemoryStack stack = stackPush()) {
	        VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
	        LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
	        
	        layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
	        layoutInfo.pBindings(bindings);

	        if(VK_SUCCESS == vkCreateDescriptorSetLayout(logicalDevice.device(), layoutInfo, null, pDescriptorSetLayout)) {
	            descriptorSetLayout = pDescriptorSetLayout.get(0);	
	        } else {
	            throw new RuntimeException("Failed to create descriptor set layout");
	        }	
		}
	}
}

package renderer.vulkan.descriptor.setLayouts;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

import renderer.vulkan.LogicalDevice;

public class DescriptorSetLayoutSingleUniform extends DescriptorSetLayout {
    /*
     * constructors
     */
	public DescriptorSetLayoutSingleUniform() {
		super();
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice) {
		try(MemoryStack stack = stackPush()) {
	        VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(1, stack);
	        VkDescriptorSetLayoutBinding uboLayoutBinding = bindings.get(0);
	        
	        uboLayoutBinding.binding(0);
	        uboLayoutBinding.descriptorCount(1);
	        uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
	        uboLayoutBinding.pImmutableSamplers(null);
	        uboLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

	        super.create(logicalDevice, bindings);	
		}
    }
}

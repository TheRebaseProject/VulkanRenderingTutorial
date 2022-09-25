package renderer.vulkan.descriptor;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import renderer.vulkan.LogicalDevice;
import renderer.vulkan.buffer.uniform.UniformBufferUniformColor;

public class DescriptorUniformColor extends Descriptor {
    /*
     * constructors
     */
	public DescriptorUniformColor() {
		super();
	}
	
	/*
	 * public methods
	 */
	public void updateDescriptorSet(LogicalDevice logicalDevice, long descriptorSet, UniformBufferUniformColor uboColor) {
		try(MemoryStack stack = stackPush()) {
	        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
	        VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.calloc(1, stack);
	        VkWriteDescriptorSet writeDescriptorSetUBO = writeDescriptorSets.get(0);
	        
	        descriptorBufferInfo.offset(0);
	        descriptorBufferInfo.range(uboColor.getSize());
	        descriptorBufferInfo.buffer(uboColor.getBuffer());

	        writeDescriptorSetUBO.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
	        writeDescriptorSetUBO.dstBinding(0);
	        writeDescriptorSetUBO.dstArrayElement(0);
	        writeDescriptorSetUBO.dstSet(descriptorSet);
	        writeDescriptorSetUBO.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
	        writeDescriptorSetUBO.descriptorCount(1);
	        writeDescriptorSetUBO.pBufferInfo(descriptorBufferInfo);
	        
	        vkUpdateDescriptorSets(logicalDevice.device(), writeDescriptorSets, null);
		}
	}

	/*
	 * protected methods
	 */
	@Override
    protected void createDescriptorPool(LogicalDevice logicalDevice) {
        try(MemoryStack stack = stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(1, stack);
            VkDescriptorPoolSize uniformBufferPoolSize = poolSizes.get(0);
            
            uniformBufferPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uniformBufferPoolSize.descriptorCount(1);

            descriptorPool.create(logicalDevice, poolSizes, maxSets, DescriptorPool.NO_CREATE_FLAGS);
        }
    }
}

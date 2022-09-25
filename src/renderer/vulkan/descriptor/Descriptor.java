package renderer.vulkan.descriptor;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;
import java.util.ArrayList;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import renderer.vulkan.LogicalDevice;
import renderer.vulkan.descriptor.setLayouts.DescriptorSetLayout;

public abstract class Descriptor {
	/*
	 * protected variables
	 */
	protected DescriptorPool descriptorPool;
	protected ArrayList<Long> descriptorSets;
	protected int maxSets;
	
	/*
	 * constructors
	 */
	public Descriptor() {
		descriptorPool = new DescriptorPool();
		descriptorSets = new ArrayList<Long>(maxSets);
		this.maxSets = 0;
	}
	
	/*
	 * public methods
	 */
	public void allocateDescriptorSets(LogicalDevice logicalDevice, DescriptorSetLayout descriptorSetLayout) {
        try(MemoryStack stack = stackPush()) {
            LongBuffer layouts = stack.mallocLong(maxSets);
            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack);
            LongBuffer pDescriptorSets = stack.mallocLong(maxSets);
            
            for(int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout.getDescriptorSetLayout());
            }
            
            allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool(descriptorPool.getDescriptorPool());
            allocInfo.pSetLayouts(layouts);

            if(VK_SUCCESS == vkAllocateDescriptorSets(logicalDevice.device(), allocInfo, pDescriptorSets)) {
                for(int i = 0; i < pDescriptorSets.capacity(); i++) {
                    descriptorSets.add(pDescriptorSets.get(i));	
                }
            } else {
                throw new RuntimeException("Failed to allocate descriptor sets");
            }
        }
	}
	
	public void create(LogicalDevice logicalDevice, int maxSets) {
		this.maxSets = maxSets;
		if(0 < this.maxSets) {createDescriptorPool(logicalDevice);}
	}
	
	public void destroy(LogicalDevice logicalDevice) {
		descriptorSets.clear();
		descriptorPool.destroy(logicalDevice);
	}
	
	public long getDescriptorSet(int index) {return descriptorSets.get(index);}
	public boolean isInitialized() {return VK_NULL_HANDLE != descriptorPool.getDescriptorPool();}
	
	/*
	 * protected methods
	 */
	protected abstract void createDescriptorPool(LogicalDevice logicalDevice);
}

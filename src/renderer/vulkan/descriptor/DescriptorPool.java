package renderer.vulkan.descriptor;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import renderer.vulkan.LogicalDevice;

public class DescriptorPool {
	/*
	 * public class constants
	 */
	public static int NO_CREATE_FLAGS = 0;
	
    /*
     * private variables
     */
	private long descriptorPool;
	
	/*
	 * constructor
	 */
	public DescriptorPool() {
		descriptorPool = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, VkDescriptorPoolSize.Buffer poolSizes, int maxSets, int flags) {
		try(MemoryStack stack = stackPush()) {
			if(VK_NULL_HANDLE == descriptorPool) {
		        VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack);
		        LongBuffer pDescriptorPool = stack.mallocLong(1);
		        
		        poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
		        poolInfo.flags(flags);
		        poolInfo.pPoolSizes(poolSizes);
		        poolInfo.maxSets(maxSets);

		        if(VK_SUCCESS == vkCreateDescriptorPool(logicalDevice.device(), poolInfo, null, pDescriptorPool)) {
		        	descriptorPool = pDescriptorPool.get(0);
		        } else {
		            throw new RuntimeException("Failed to create descriptor pool");
		        }	
			} else {
				throw new RuntimeException("Descriptor pool already allocated");
		    }
		}
	}
	
	public void destroy(LogicalDevice logicalDevice) {
		vkDestroyDescriptorPool(logicalDevice.device(), descriptorPool, null);
		descriptorPool = VK_NULL_HANDLE;
	}
	
	public long getDescriptorPool() {return descriptorPool;}
}

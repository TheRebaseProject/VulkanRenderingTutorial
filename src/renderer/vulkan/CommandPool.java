package renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import renderer.vulkan.physicalDevice.PhysicalDevice;
import renderer.vulkan.physicalDevice.QueueFamilyIndices;

public class CommandPool {
	/*
	 * private variables
	 */
	private long commandPool;

    /*
     * constructors
     */
	public CommandPool() {
		commandPool = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
    public void create(PhysicalDevice physicalDevice, LogicalDevice logicalDevice, int flags) {
        try(MemoryStack stack = stackPush()) {
            QueueFamilyIndices queueFamilyIndices = physicalDevice.getQueueFamilyIndicies();
            VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
            LongBuffer pCommandPool = stack.mallocLong(1);
            
            commandPoolCreateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            commandPoolCreateInfo.flags(flags);
            commandPoolCreateInfo.queueFamilyIndex(queueFamilyIndices.getGraphicsFamily());

            if(VK_SUCCESS == vkCreateCommandPool(logicalDevice.device(), commandPoolCreateInfo, null, pCommandPool)) {
                commandPool = pCommandPool.get(0);
            } else {
                throw new RuntimeException("Failed to create command pool");
            }
        }
    }
    
    public long commandPool() {return commandPool;}
    public void destroy(LogicalDevice logicalDevice) {vkDestroyCommandPool(logicalDevice.device(), commandPool, null);}
}

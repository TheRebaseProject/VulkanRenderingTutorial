package renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSubmitInfo;

public class CommandBuffer {
	/*
	 * private variables
	 */
	VkCommandBuffer commandBuffer;
	CommandPool commandPool;
	
    /*
     * constructors
     */
	public CommandBuffer(CommandPool commandPool) {
		commandBuffer = null;
		this.commandPool = commandPool;
	}
	
	/*
	 * public methods
	 */
	public void allocate(LogicalDevice logicalDevice) {
		try(MemoryStack stack = stackPush()) {
	        VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
	        PointerBuffer pCommandBuffer = stack.mallocPointer(1);
	        VkDevice device = logicalDevice.device();
	        
	        allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
	        allocInfo.commandPool(commandPool.commandPool());
	        allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	        allocInfo.commandBufferCount(1);

	        if(VK_SUCCESS == vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer)) {
	        	commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);
	        } else {
	            throw new RuntimeException("Fialed to allocate command buffer");
	        }		
		}
	}
	
    public void beginOneTimeCommandBuffer(LogicalDevice logicalDevice) {
        try(MemoryStack stack = stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            
            allocate(logicalDevice);

            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            if(VK_SUCCESS != vkBeginCommandBuffer(commandBuffer, beginInfo)) {
            	throw new RuntimeException("Fialed to begin one time command buffer");
            }
        }
    }
    
    public void freeCommandBuffer(LogicalDevice logicalDevice) {
    	vkFreeCommandBuffers(logicalDevice.device(), commandPool.commandPool(), commandBuffer);
    }
    
    public VkCommandBuffer getCommandBuffer() {return commandBuffer;}

    public void submitOneTimeCommandBuffer(LogicalDevice logicalDevice) {
        try(MemoryStack stack = stackPush()) {
            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));
            
            if(VK_SUCCESS == vkEndCommandBuffer(commandBuffer) &&
               VK_SUCCESS == vkQueueSubmit(logicalDevice.getGraphicsQueue(), submitInfo, VK_NULL_HANDLE) &&
               VK_SUCCESS == vkQueueWaitIdle(logicalDevice.getGraphicsQueue())) {
            	freeCommandBuffer(logicalDevice);
            } else {
            	throw new RuntimeException("Fialed to submit one time command buffer");
            }           
        }
    }
}

package renderer.vulkan.buffer.transfer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;

import renderer.vulkan.CommandBuffer;
import renderer.vulkan.CommandPool;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.buffer.BufferObject;
import utilities.MemoryUtilities;

public abstract class TransferBufferObject extends BufferObject {
    /*
     * private variables
     */
	private int usage;
	
	/*
	 * constructors
	 */
	public TransferBufferObject(int usage) {
		super();
		
		this.usage = usage;
	}
	
	/*
	 * protected methods
	 */
    protected void create(LogicalDevice logicalDevice, long allocator, CommandPool commandPool, long bufferSize) {
        try(MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pBufferMemory = stack.mallocPointer(1);
            long stagingBuffer;
            long stagingBufferMemory;
            PointerBuffer ptr = stack.mallocPointer(1);
            
            MemoryUtilities.createBuffer(logicalDevice,
            		allocator,
            		bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VMA_MEMORY_USAGE_CPU_ONLY,//VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
                    pBuffer,
                    pBufferMemory);

            stagingBuffer = pBuffer.get(0);
            stagingBufferMemory = pBufferMemory.get(0);

            vmaMapMemory(allocator, stagingBufferMemory, ptr);
            memcpy(ptr, bufferSize);
            vmaUnmapMemory(allocator, stagingBufferMemory);
            vmaFlushAllocation(allocator, stagingBufferMemory, 0, bufferSize);

            MemoryUtilities.createBuffer(logicalDevice,
            		allocator,
            		bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage,
                    VMA_MEMORY_USAGE_GPU_ONLY,//VK_MEMORY_HEAP_DEVICE_LOCAL_BIT
                    pBuffer,
                    pBufferMemory);

            buffer = pBuffer.get(0);
            bufferMemory = pBufferMemory.get(0);
            
            copyBuffer(logicalDevice, commandPool, stagingBuffer, buffer, bufferSize);
            vmaDestroyBuffer(allocator, stagingBuffer, stagingBufferMemory);
        }
    }
    
	protected abstract void memcpy(PointerBuffer ptr, long bufferSize);
	
	/*
	 * private methods
	 */
    private void copyBuffer(LogicalDevice logicalDevice, CommandPool commandPool, long srcBuffer, long dstBuffer, long size) {
        try(MemoryStack stack = stackPush()) {
            CommandBuffer commandBuffer =  new CommandBuffer(commandPool);
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
            
            commandBuffer.beginOneTimeCommandBuffer(logicalDevice);
            copyRegion.size(size);
            vkCmdCopyBuffer(commandBuffer.getCommandBuffer(), srcBuffer, dstBuffer, copyRegion);
            commandBuffer.submitOneTimeCommandBuffer(logicalDevice);
        }
    }
}

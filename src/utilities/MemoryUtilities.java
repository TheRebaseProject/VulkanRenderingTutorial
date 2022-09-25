package utilities;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.vmaCreateBuffer;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import renderer.vulkan.LogicalDevice;

public class MemoryUtilities {
    /*
     * public class methods
     */
    public static PointerBuffer asPointerBuffer(String[] strings) {
        MemoryStack stack = stackGet();
        PointerBuffer pointerBuffer = stack.mallocPointer(strings.length);
        
        for(int i = 0; i < strings.length; i++) {
        	ByteBuffer byteBuffer = stack.UTF8(strings[i]);
        	pointerBuffer.put(byteBuffer);
        }

        return pointerBuffer.rewind();
    }
    
    public static void createBuffer(LogicalDevice logicalDevice, long allocator, long size, int usage, int vmaUsage, LongBuffer pBuffer, PointerBuffer pBufferMemory) {
        try(MemoryStack stack = stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
            VmaAllocationCreateInfo vmaAllocationCI = VmaAllocationCreateInfo.calloc(stack);
            VmaAllocationInfo pAllocationInfo = VmaAllocationInfo.malloc(stack);
            
            bufferCreateInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferCreateInfo.size(size);
            bufferCreateInfo.usage(usage);
            bufferCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            vmaAllocationCI.usage(vmaUsage);
            
            if(VK_SUCCESS != vmaCreateBuffer(allocator, bufferCreateInfo, vmaAllocationCI, pBuffer, pBufferMemory, pAllocationInfo)) {
            	throw new RuntimeException("Failed to create VMA buffer");
            }
        }
    }
}

package renderer.vulkan.buffer.uniform;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.util.vma.Vma.*;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import renderer.vulkan.LogicalDevice;
import renderer.vulkan.buffer.BufferObject;
import utilities.MemoryUtilities;

public abstract class UniformBufferObject extends BufferObject {
	/*
	 * protected class constants
	 */
	protected static final byte BYTES_PER_MATRIX_4F = 16 * Float.BYTES;
	
	/*
	 * protected variables
	 */
	protected int size;
    
    /*
     * constructors
     */
    public UniformBufferObject() {
    	super();
    	
    	size = 0;
    }
    
    /*
     * public methods
     */
    public void create(LogicalDevice logicalDevice, long allocator) {
    	create(logicalDevice, allocator, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, getSize());
    }
    
    public void create(LogicalDevice logicalDevice, long allocator, int usage, int size) {
        try(MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pBufferMemory = stack.mallocPointer(1);
           	
            MemoryUtilities.createBuffer(logicalDevice,
            		allocator,
            		size,
                    usage,
                    VMA_MEMORY_USAGE_CPU_TO_GPU,//VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
                    pBuffer,
                    pBufferMemory);

            buffer = pBuffer.get(0);
            bufferMemory = pBufferMemory.get(0);
            this.size = size;
        }
    }
    
    public int getSize() {return size;}
}

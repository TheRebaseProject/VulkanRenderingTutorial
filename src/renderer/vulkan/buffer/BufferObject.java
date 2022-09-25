package renderer.vulkan.buffer;

import static org.lwjgl.util.vma.Vma.vmaDestroyBuffer;
import static org.lwjgl.vulkan.VK10.*;

import renderer.vulkan.LogicalDevice;

public abstract class BufferObject {
	/*
	 * protected variables
	 */
    protected long buffer;
    protected long bufferMemory;
    
    /*
     * constructors
     */
    public BufferObject() {
    	buffer = VK_NULL_HANDLE;
    	bufferMemory = VK_NULL_HANDLE;
    }
    
    /*
     * public methods
     */
	public void destroy(LogicalDevice logicalDevice, long allocator) {
		vmaDestroyBuffer(allocator, buffer, bufferMemory);
	}
	
	public long getBuffer() {return buffer;}
}

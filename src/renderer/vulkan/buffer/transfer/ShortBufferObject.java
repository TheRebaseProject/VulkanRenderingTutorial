package renderer.vulkan.buffer.transfer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;

import java.nio.ShortBuffer;

import org.lwjgl.PointerBuffer;

import renderer.vulkan.CommandPool;
import renderer.vulkan.LogicalDevice;

public class ShortBufferObject extends TransferBufferObject {
    /*
     * private variables
     */
	private short[] data;
	
	/*
	 * constructors
	 */
	public ShortBufferObject() {
		super(VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		
		this.data = null;
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, long allocator, CommandPool commandPool, short[] data) {
		this.data = data;
		create(logicalDevice, allocator, commandPool, data.length * Short.BYTES);
		this.data = null;
	}
	
	/*
	 * protected methods
	 */
	@Override
	protected void memcpy(PointerBuffer ptr, long bufferSize) {
        ShortBuffer shortBuffer = ptr.getShortBuffer(0, (int)(bufferSize / Short.BYTES));
        for(int i = 0; i < data.length; i++) {shortBuffer.put(i, data[i]);}
	}
}

package renderer.vulkan.buffer.transfer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

import java.nio.FloatBuffer;

import org.lwjgl.PointerBuffer;

import renderer.vulkan.CommandPool;
import renderer.vulkan.LogicalDevice;

public class QuadBufferObject extends TransferBufferObject {
    /*
     * private variables
     */
	private float[] positions;
	private float[] texelCoords;
	
	/*
	 * constructors
	 */
	public QuadBufferObject() {
		super(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		
		positions = null;
		texelCoords = null;
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, long allocator, CommandPool commandPool, float[] positions, float[] texelCoords) {
		long bufferSize = (positions.length + texelCoords.length) * Float.BYTES;
		
		this.positions = positions;
		this.texelCoords = texelCoords;
		create(logicalDevice, allocator, commandPool, bufferSize);
		this.positions = null;
		this.texelCoords = null;
	}
	
	/*
	 * protected methods
	 */
	@Override
	protected void memcpy(PointerBuffer ptr, long bufferSize) {
		final byte STRIDE = 2 + 2;
        FloatBuffer floatBuffer = ptr.getFloatBuffer(0, (int)(bufferSize / Float.BYTES));
        
        for(int i = 0; i < positions.length / 2; i++) {
      	    floatBuffer.put((i * STRIDE) + 0, positions[(i * 2) + 0]);
      	    floatBuffer.put((i * STRIDE) + 1, positions[(i * 2) + 1]);
      	    
      	    floatBuffer.put((i * STRIDE + 2) + 0, texelCoords[(i * 2) + 0]);
      	    floatBuffer.put((i * STRIDE + 2) + 1, texelCoords[(i * 2) + 1]);
        }
	}
}

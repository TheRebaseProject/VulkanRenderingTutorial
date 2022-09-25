package renderer.vulkan.buffer.uniform;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;

import java.nio.ByteBuffer;

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import renderer.vulkan.LogicalDevice;

public class UniformBufferUniformColor extends UniformBufferObject {	
    /*
     * private class constants
     */
    private static final int SIZE = 4 * Float.BYTES;

    /*
     * constructors
     */
    public UniformBufferUniformColor() {
    	super();
    }
    
	/*
	 * public methods
	 */	
	@Override
	public int getSize() {return SIZE;}
	
    public void update(LogicalDevice logicalDevice, long allocator, Vector4f color) {
        try(MemoryStack stack = stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);

            vmaMapMemory(allocator, bufferMemory, ptr);
            
            ByteBuffer byteBuffer = ptr.getByteBuffer(0, SIZE);
            color.get(0, byteBuffer);
            
            vmaUnmapMemory(allocator, bufferMemory);
        }
    }
}

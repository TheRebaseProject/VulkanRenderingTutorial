package renderer.vulkan.vertexData;

import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;

import renderer.vulkan.LogicalDevice;
import renderer.vulkan.buffer.transfer.TransferBufferObject;

public abstract class VertexData {
	/*
	 * protected variables
	 */
    protected TransferBufferObject indexBufferObject;
    protected int indexType;
    protected int indicesCount;
    protected TransferBufferObject vertexBufferObject;
	
    /*
     * constructors
     */
	public VertexData() {
        indexBufferObject = null;
        indicesCount = 0;
        indexType = VK_INDEX_TYPE_UINT32;
        vertexBufferObject = null;
	}
	
	/*
	 * public methods
	 */
	public void destroy(LogicalDevice logicalDevice, long allocator) {
		indexBufferObject.destroy(logicalDevice, allocator);      
		vertexBufferObject.destroy(logicalDevice, allocator);
	}
	
	public long indexBuffer() {return indexBufferObject.getBuffer();}
	public int indexType() {return indexType;}
	public int indicesCount() {return indicesCount;}
	public long vertexBuffer() {return vertexBufferObject.getBuffer();}
}

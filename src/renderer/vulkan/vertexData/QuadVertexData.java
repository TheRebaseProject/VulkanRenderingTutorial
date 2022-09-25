package renderer.vulkan.vertexData;

import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import renderer.vulkan.CommandPool;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.buffer.transfer.QuadBufferObject;
import renderer.vulkan.buffer.transfer.ShortBufferObject;

public class QuadVertexData extends VertexData {	
	/*
	 * public class methods
	 */
    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1);
        final int BINDING_0_STRIDE = 4 * Float.BYTES;

        bindingDescription.binding(0);
        bindingDescription.stride(BINDING_0_STRIDE);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
        final int LOCATION_0_OFFSET = 0;
        final int LOCATION_1_OFFSET = 2 * Float.BYTES;

        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32_SFLOAT);
        posDescription.offset(LOCATION_0_OFFSET);
        
        VkVertexInputAttributeDescription texCoordsDescription = attributeDescriptions.get(1);
        texCoordsDescription.binding(0);
        texCoordsDescription.location(1);
        texCoordsDescription.format(VK_FORMAT_R32G32_SFLOAT);
        texCoordsDescription.offset(LOCATION_1_OFFSET);

        return attributeDescriptions.rewind();
    }
    
	/*
	 * private class constants
	 */    
    private static final float[] POSITIONS = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f};
    private static final float[] TEXEL_COORDS = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    private static final short[] INDICES = {0, 1, 3, 3, 1, 2};
	
	/*
	 * constructors
	 */
	public QuadVertexData() {
		super();
		
		indexBufferObject = new ShortBufferObject();
		indicesCount = INDICES.length;
		indexType = VK_INDEX_TYPE_UINT16;
		vertexBufferObject = new QuadBufferObject();
	}
	
	/*
	 * public methods
	 */	
	public void init(LogicalDevice logicalDevice, long allocator, CommandPool commandPool) {
		((ShortBufferObject)indexBufferObject).create(logicalDevice, allocator, commandPool, INDICES);
		((QuadBufferObject)vertexBufferObject).create(logicalDevice, allocator, commandPool, POSITIONS, TEXEL_COORDS);
	}
}

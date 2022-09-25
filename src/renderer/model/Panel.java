package renderer.model;

import static org.lwjgl.vulkan.VK10.*;

import renderer.Renderer;
import renderer.vulkan.CommandPool;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.vertexData.QuadVertexData;

public class Panel extends Model {
    /*
     * constructors
     */
	public Panel() {
		super();
		
		vertexData = new QuadVertexData();
	}
	
	/*
	 * public methods
	 */
	@Override
	public void deinit(Renderer renderer) {
		vertexData.destroy(renderer.getLogicalDevice(), renderer.getMemoryAllocator());
	}

	@Override
	public void init(Renderer renderer) {
		LogicalDevice logicalDevice = renderer.getLogicalDevice();
		CommandPool commandPool = new CommandPool();
		
		commandPool.create(logicalDevice.getPhysicalDevice(), logicalDevice, VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
		
		((QuadVertexData)vertexData).init(renderer.getLogicalDevice(), renderer.getMemoryAllocator(), commandPool);
		
		commandPool.destroy(renderer.getLogicalDevice());
	}
}

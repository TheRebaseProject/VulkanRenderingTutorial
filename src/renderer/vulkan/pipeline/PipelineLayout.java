package renderer.vulkan.pipeline;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import renderer.vulkan.LogicalDevice;

public class PipelineLayout {
	/*
	 * private variables
	 */
	private long pipelineLayout;
	
    /*
     * constructors
     */
	public PipelineLayout()	{
		pipelineLayout = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
	public void create(LogicalDevice logicalDevice, VkPipelineLayoutCreateInfo pipelineLayoutInfo) {
		try(MemoryStack stack = stackPush()) {
			LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
			
	        if(VK_SUCCESS == vkCreatePipelineLayout(logicalDevice.device(), pipelineLayoutInfo, null, pPipelineLayout)) {
	            pipelineLayout = pPipelineLayout.get(0);
	        } else {
	            throw new RuntimeException("Failed to create pipeline layout");
	        }
		}
	}
	
	public void destroy(LogicalDevice logicalDevice) {
		vkDestroyPipelineLayout(logicalDevice.device(), pipelineLayout, null);
	}
	
	public long getPipelineLayout() {return pipelineLayout;}
}

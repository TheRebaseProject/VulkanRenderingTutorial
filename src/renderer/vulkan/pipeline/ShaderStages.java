package renderer.vulkan.pipeline;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import renderer.vulkan.LogicalDevice;

public abstract class ShaderStages {
	/*
	 * protected class constants
	 */
	protected static final String SHADER_PATH_BASE = "renderer/vulkan/pipeline/";
	
	/*
	 * constructors
	 */
	public ShaderStages() {}
	
	/*
	 * public methods
	 */
	public abstract void destroy(LogicalDevice logicalDevice);	
	public abstract VkPipelineShaderStageCreateInfo.Buffer shaderStageCreateInfos(LogicalDevice logicalDevice, MemoryStack stack);
	
	/*
	 * protected methods
	 */
    protected long createShaderModule(LogicalDevice logicalDevice, ByteBuffer spirvCode) {
        try(MemoryStack stack = stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
            LongBuffer pShaderModule = stack.mallocLong(1);

            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            if(VK_SUCCESS != vkCreateShaderModule(logicalDevice.device(), createInfo, null, pShaderModule)) {
                throw new RuntimeException("Failed to create shader module");
            }

            return pShaderModule.get(0);
        }
    }
}

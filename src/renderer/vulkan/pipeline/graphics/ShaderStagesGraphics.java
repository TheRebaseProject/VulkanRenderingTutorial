package renderer.vulkan.pipeline.graphics;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import renderer.vulkan.LogicalDevice;
import renderer.vulkan.pipeline.ShaderSPIRV;
import renderer.vulkan.pipeline.ShaderStages;

public abstract class ShaderStagesGraphics extends ShaderStages {
	/*
	 * protected class constants
	 */
	protected static final String SHADER_PATH_VERTEX = SHADER_PATH_BASE + "graphics/texture2d/texture2d.vert";
	
    /*
     * private class constants
     */
	private static final byte SHADER_STAGES_COUNT = 2;
	
	/*
	 * protected variables
	 */
	protected long fragmentShaderModule;
	protected ShaderSPIRV fragmentShaderSPIRV;
	protected long vertexShaderModule;
	protected ShaderSPIRV vertexShaderSPIRV;
	
	/*
	 * constructors
	 */
	public ShaderStagesGraphics() {
        fragmentShaderModule = VK_NULL_HANDLE;
        fragmentShaderSPIRV = new ShaderSPIRV();
        vertexShaderModule = VK_NULL_HANDLE;
        vertexShaderSPIRV = new ShaderSPIRV();
	}
	
	/*
	 * public methods
	 */
	@Override
	public void destroy(LogicalDevice logicalDevice) {
        vkDestroyShaderModule(logicalDevice.device(), vertexShaderModule, null);
        vkDestroyShaderModule(logicalDevice.device(), fragmentShaderModule, null);

        vertexShaderSPIRV.free();
        fragmentShaderSPIRV.free();
	}
	
	@Override
	public VkPipelineShaderStageCreateInfo.Buffer shaderStageCreateInfos(LogicalDevice logicalDevice, MemoryStack stack) {
        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(SHADER_STAGES_COUNT, stack);
        VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
        VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
        ByteBuffer entryPoint = stack.UTF8("main");
        
	    vertexShaderSPIRV.compileShaderFile(getShaderPathVertex(), ShaderSPIRV.TYPE_VERTEX_SHADER);
	    fragmentShaderSPIRV.compileShaderFile(getShaderPathFragment(), ShaderSPIRV.TYPE_FRAGMENT_SHADER);

	    vertexShaderModule = createShaderModule(logicalDevice, vertexShaderSPIRV.bytecode());
	    fragmentShaderModule = createShaderModule(logicalDevice, fragmentShaderSPIRV.bytecode());

        vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
        vertShaderStageInfo.module(vertexShaderModule);
        vertShaderStageInfo.pName(entryPoint);

        fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
        fragShaderStageInfo.module(fragmentShaderModule);
        fragShaderStageInfo.pName(entryPoint);
        
        return shaderStages;
	}
	
	/*
	 * protected methods
	 */    
    protected abstract String getShaderPathVertex();
    protected abstract String getShaderPathFragment();
}

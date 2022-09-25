package renderer.vulkan.pipeline.graphics.uniformColor;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import renderer.Renderer;
import renderer.vulkan.pipeline.graphics.GraphicsPipeline;

public class GraphicsPipelineUniformColor extends GraphicsPipeline {
	/*
	 * private class constants
	 */
	private static final short PUSH_CONSTANT_SIZE = 0;
	private static final int PUSH_CONSTANT_STAGES = 0;
	
    /*
     * constructors
     */
	public GraphicsPipelineUniformColor() {
		super();
		shaderStages = new ShaderStagesUniformColor();
	}
	
	/*
	 * public methods
	 */	
	@Override
	public short getPushConstantSize() {return PUSH_CONSTANT_SIZE;}
	
	@Override
	public int getPushConstantStages() {return PUSH_CONSTANT_STAGES;}
	
	/*
	 * protected methods
	 */	
	@Override
	protected void createPipelineLayout(Renderer renderer) {
		try(MemoryStack stack = stackPush()) {
	        VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
	        LongBuffer descriptorSetLayouts = stack.callocLong(1);
	        
	        descriptorSetLayouts.put(0, renderer.getDescriptorSetLayout(Renderer.DESCRIPTOR_SET_LAYOUT_SINGLE_UNIFORM).getDescriptorSetLayout());
	
	        pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
	        pipelineLayoutInfo.pSetLayouts(descriptorSetLayouts);
	
	        pipelineLayout.create(renderer.getLogicalDevice(), pipelineLayoutInfo);
		}
    }
}

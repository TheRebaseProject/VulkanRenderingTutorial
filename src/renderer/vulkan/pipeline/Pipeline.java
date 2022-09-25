package renderer.vulkan.pipeline;

import static org.lwjgl.vulkan.VK10.*;

import renderer.Renderer;
import renderer.vulkan.LogicalDevice;

public abstract class Pipeline {
	/*
	 * protected variables
	 */
	protected long pipeline;
	protected PipelineLayout pipelineLayout;
	protected ShaderStages shaderStages;
    
	/*
     * constructors
     */
	public Pipeline() {
		pipeline = VK_NULL_HANDLE;
		pipelineLayout = new PipelineLayout();
		shaderStages = null;
	}
	
	/*
	 * public methods
	 */    
    public void destroy(LogicalDevice logicalDevice) {
        vkDestroyPipeline(logicalDevice.device(), pipeline, null);
        pipelineLayout.destroy(logicalDevice);
    }
    
    public long getPipeline() {return pipeline;}
    public PipelineLayout getPipelineLayout() {return pipelineLayout;}
    public abstract short getPushConstantSize();
    public abstract int getPushConstantStages();
    
    /*
     * protected methods
     */
	protected abstract void createPipelineLayout(Renderer renderer);
}

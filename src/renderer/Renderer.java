package renderer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.system.Configuration.DEBUG;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;

import main.WindowManager;
import renderer.model.Model;
import renderer.vulkan.Instance;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.MemoryAllocator;
import renderer.vulkan.descriptor.setLayouts.DescriptorSetLayout;
import renderer.vulkan.descriptor.setLayouts.DescriptorSetLayoutSingleUniform;
import renderer.vulkan.physicalDevice.PhysicalDevice;
import renderer.vulkan.pipeline.Pipeline;
import renderer.vulkan.pipeline.graphics.GraphicsPipeline;
import renderer.vulkan.pipeline.graphics.uniformColor.GraphicsPipelineUniformColor;
import renderer.vulkan.swapChain.SwapChain;

public class Renderer {
	/*
	 * public class constants
	 */
	public static final String[] DEVICE_EXTENSIONS = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};
	
    public static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
    public static final String[] VALIDATION_LAYERS = {"VK_LAYER_KHRONOS_validation"};
    
    public static final byte DESCRIPTOR_SET_LAYOUT_SINGLE_UNIFORM = 0;
    public static final byte DESCRIPTOR_SET_LAYOUT_COUNT = 1;
    
    /*
     * private class constants
     */
    private static final byte GP_UNIFORM_COLOR = 0;
    private static final byte GP_COUNT = 1;
    
	/*
	 * private variables
	 */
    private DescriptorSetLayout[] descriptorSetLayouts;
    private GraphicsPipeline[] graphicsPipelines;
	private Instance instance;
	private LogicalDevice logicalDevice;
	private MemoryAllocator memoryAllocator;
	private PhysicalDevice physicalDevice;
	private SwapChain swapChain;
    
    /*
     * constructors
     */
	public Renderer() {
		descriptorSetLayouts = new DescriptorSetLayout[DESCRIPTOR_SET_LAYOUT_COUNT];
		descriptorSetLayouts[DESCRIPTOR_SET_LAYOUT_SINGLE_UNIFORM] = new DescriptorSetLayoutSingleUniform();
		
		graphicsPipelines = new GraphicsPipeline[GP_COUNT];
		
		instance = new Instance();
		logicalDevice = new LogicalDevice();
		memoryAllocator = new MemoryAllocator();
		physicalDevice = new PhysicalDevice();
		swapChain = new SwapChain();
	}
	
	/*
	 * public methods
	 */
	public int acquireNextSwapChainImage() {
		int vkResult = swapChain.acquireNextImage(logicalDevice);
		
		if(VK_SUCCESS != vkResult && VK_ERROR_OUT_OF_DATE_KHR != vkResult) {
			throw new RuntimeException("Failed to acquire next image in swap chain");
		}
		
		return vkResult;
	}
	
    public void beginCommandBuffer() {
    	try(MemoryStack stack = stackPush()) {
			int nextImageIndex = swapChain.getNextImageIndex();
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);    			
            VkCommandBuffer commandBuffer = swapChain.getCommandBuffers().get(nextImageIndex).getCommandBuffer();
            
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            if(VK_SUCCESS != vkBeginCommandBuffer(commandBuffer, beginInfo)) {
                throw new RuntimeException("Failed to begin command buffer");
            }
    	}
    }
    
    public void beginRenderPass() {
    	int nextImageIndex = swapChain.getNextImageIndex();
    	VkCommandBuffer commandBuffer = swapChain.getCommandBuffers().get(nextImageIndex).getCommandBuffer();
    	
	    swapChain.getRenderPass().beginRenderPass(swapChain.getImageExtent().height(),
	    		swapChain.getImageExtent().width(),
	    		swapChain.getFramebuffers().get(nextImageIndex).framebuffer(),
	    		commandBuffer);
    }
	
	public void create() {
		instance.create();
		physicalDevice.selectPhysicalDevice(instance.getInstance());
		logicalDevice.create(physicalDevice);
		memoryAllocator.create(instance, physicalDevice, logicalDevice);
		
		createDescriptorSetLayouts();
		
        createSwapChainObjects();
	}
	
	public void destroy() {
		destroySwapChainObjects();
		
		destroyDescriptorSetLayouts();
		
		memoryAllocator.destroy();
		logicalDevice.destroy();
		//no resources released by a PhysicalDevice
		instance.destroy();
	}
	
	public void drawUniformColor(long descriptorSet, Model model) {
    	try(MemoryStack stack = stackPush()) {
    		GraphicsPipelineUniformColor gp = (GraphicsPipelineUniformColor)graphicsPipelines[GP_UNIFORM_COLOR];
            
            addDrawCommands(stack.longs(descriptorSet), gp, null, model, 1);
    	}
	}
	
    public void endRenderPass() {
    	int nextImageIndex = swapChain.getNextImageIndex();
    	VkCommandBuffer commandBuffer = swapChain.getCommandBuffers().get(nextImageIndex).getCommandBuffer();
    	
        vkCmdEndRenderPass(commandBuffer);
    }
	
	public DescriptorSetLayout getDescriptorSetLayout(byte dsl) {return descriptorSetLayouts[dsl];}
	public LogicalDevice getLogicalDevice() {return logicalDevice;}
	public long getMemoryAllocator() {return memoryAllocator.allocator();}
	public int getNextImageIndex() {return swapChain.getNextImageIndex();}
	public int getSwapChainImageCount() {return swapChain.getImages().size();}
	
    public void recreateSwapChain() {
    	try(MemoryStack stack = stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);

            while(width.get(0) == 0 && height.get(0) == 0) {
                glfwGetFramebufferSize(WindowManager.getWindow(), width, height);
                glfwWaitEvents();
            }
            
            waitForDeviceIdle();
            destroySwapChainObjects();
            createSwapChainObjects();
        }
    }
    
    public boolean submit() {
    	boolean recreateSwapChain = false;
    	int nextImageIndex = swapChain.getNextImageIndex();
    	VkCommandBuffer commandBuffer = swapChain.getCommandBuffers().get(nextImageIndex).getCommandBuffer();

        if(VK_SUCCESS == vkEndCommandBuffer(commandBuffer)) {        	
        	if(VK_SUCCESS == swapChain.submitDrawCommand(logicalDevice, commandBuffer)) {
            	int vkResult = swapChain.present(logicalDevice);

                if(VK_ERROR_OUT_OF_DATE_KHR == vkResult || VK_SUBOPTIMAL_KHR == vkResult || WindowManager.framebufferResized()) {
                    WindowManager.acknowledgeResize();
                    recreateSwapChain();
                    recreateSwapChain = true;
                } else if(VK_SUCCESS != vkResult) {
                    throw new RuntimeException("Failed to present swap chain image");
                }
        	} else {
        		throw new RuntimeException("Failed to submit command buffer");
        	}
        } else {
            throw new RuntimeException("Failed to end command buffer");
        }
        
        return recreateSwapChain;
    }
    
	public void waitForDeviceIdle() {vkDeviceWaitIdle(logicalDevice.device());}
	
	/*
	 * private methods
	 */
    private void addDrawCommands(LongBuffer descriptorSets, Pipeline gp, ByteBuffer pc, Model model, int instanceCount) {
    	try(MemoryStack stack = stackPush()) {
        	int nextImageIndex = swapChain.getNextImageIndex();
        	VkCommandBuffer commandBuffer = swapChain.getCommandBuffers().get(nextImageIndex).getCommandBuffer();
            
        	vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gp.getPipeline());
            
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gp.getPipelineLayout().getPipelineLayout(), 0, descriptorSets, null);
            
            if(null != pc) {
            	vkCmdPushConstants(commandBuffer, gp.getPipelineLayout().getPipelineLayout(), gp.getPushConstantStages(), 0, pc);	
            }            
            
            LongBuffer vertexBuffers = stack.longs(model.getVertexData().vertexBuffer());
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
            vkCmdBindIndexBuffer(commandBuffer, model.getVertexData().indexBuffer(), 0, model.getVertexData().indexType());
            
            vkCmdDrawIndexed(commandBuffer, model.getVertexData().indicesCount(), instanceCount, 0, 0, 0);
    	}
    }
    
    private void createDescriptorSetLayouts() {
    	for(byte i = 0; i < DESCRIPTOR_SET_LAYOUT_COUNT; i++) {
    		descriptorSetLayouts[i].create(logicalDevice);
    	}
    }
    
    private void createGraphicsPipelines() {   	
		graphicsPipelines[GP_UNIFORM_COLOR] = new GraphicsPipelineUniformColor();
		graphicsPipelines[GP_UNIFORM_COLOR].create(this, swapChain.getRenderPass());
	}
    
    private void createSwapChainObjects() {
		swapChain.create(physicalDevice, logicalDevice);
		
		createGraphicsPipelines();
    }
    
    private void destroyDescriptorSetLayouts() {
    	for(byte i = 0; i < DESCRIPTOR_SET_LAYOUT_COUNT; i++) {
    		descriptorSetLayouts[i].destroy(logicalDevice);
    	}
    }
    
    private void destroyGraphicsPipelines() {
    	for(byte i = 0; i < graphicsPipelines.length; i++) {
		    graphicsPipelines[i].destroy(logicalDevice);
		}
    }
    
    private void destroySwapChainObjects() {
		destroyGraphicsPipelines();
		
		swapChain.destroy(logicalDevice);
    }
}

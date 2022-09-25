package main;

import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;

import java.util.ArrayList;

import org.joml.Vector3f;
import org.joml.Vector4f;

import renderer.Renderer;
import renderer.model.Panel;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.buffer.uniform.UniformBufferUniformColor;
import renderer.vulkan.descriptor.DescriptorUniformColor;

public class Application {
	/*
	 * private variables
	 */
	private Vector4f color;
	private DescriptorUniformColor descriptorUniformColor;
	private Vector3f directions;
	private Panel panel;
	private ArrayList<UniformBufferUniformColor> uniformColors;
	
    /*
     * constructors
     */
	public Application() {
		color = new Vector4f(0, 0, 0, 1);
		descriptorUniformColor = new DescriptorUniformColor();
		directions = new Vector3f(1);
		panel = new Panel();
		uniformColors = new ArrayList<UniformBufferUniformColor>();
	}
	
	/*
	 * public methods
	 */
	public void deinit(Renderer renderer) {
		destroySwapChainAssets(renderer);
		panel.deinit(renderer);
	}
	
	public void init(Renderer renderer) {
		panel.init(renderer);
        createSwapChainAssets(renderer);
	}
	
	public void render(Renderer renderer) {
    	int vkResult = renderer.acquireNextSwapChainImage();
    	
    	if(VK_ERROR_OUT_OF_DATE_KHR == vkResult) {
			renderer.recreateSwapChain();
			recreateSwapChainAssets(renderer);
		}
    	
    	prepareDrawAssets(renderer);
    	
    	renderer.beginCommandBuffer();
    	renderer.beginRenderPass();
    	
    	draw(renderer);
        
        renderer.endRenderPass();
    	
    	if(renderer.submit()) {
    		recreateSwapChainAssets(renderer);
    	}
	}
	
	/*
	 * private methods
	 */
	private void createSwapChainAssets(Renderer renderer) {
		LogicalDevice logicalDevice = renderer.getLogicalDevice();
		int swapChainImageCount = renderer.getSwapChainImageCount();
		long memoryAllocator = renderer.getMemoryAllocator();
		
		descriptorUniformColor.create(logicalDevice, swapChainImageCount);
		descriptorUniformColor.allocateDescriptorSets(logicalDevice, renderer.getDescriptorSetLayout(Renderer.DESCRIPTOR_SET_LAYOUT_SINGLE_UNIFORM));
		
		for(byte i = 0; i < swapChainImageCount; i++) {
			UniformBufferUniformColor uniformColor = new UniformBufferUniformColor();
			
			uniformColor.create(logicalDevice, memoryAllocator);
			uniformColors.add(uniformColor);
		}
	}
	
	private void destroySwapChainAssets(Renderer renderer) {
		LogicalDevice logicalDevice = renderer.getLogicalDevice();
		long memoryAllocator = renderer.getMemoryAllocator();

		descriptorUniformColor.destroy(logicalDevice);
		
		for(UniformBufferUniformColor uniformColor : uniformColors) {
			uniformColor.destroy(logicalDevice, memoryAllocator);
		}
		
		uniformColors.clear();
	}
	
	private void draw(Renderer renderer) {
		renderer.drawUniformColor(descriptorUniformColor.getDescriptorSet(renderer.getNextImageIndex()), panel);
	}
	
	private void prepareDrawAssets(Renderer renderer) {
		LogicalDevice logicalDevice = renderer.getLogicalDevice();
		int nextImageIndex = renderer.getNextImageIndex();
		long memoryAllocator = renderer.getMemoryAllocator();
		
		color.x += directions.x * 0.0005f;
		color.y += directions.y * 0.001f;
		color.z += directions.z * 0.002f;
		
		if(1.0f <= color.x || 0.0f >= color.x) {directions.x *= -1; color.x = Math.max(0.0f, Math.min(color.x, 1.0f));}
		if(1.0f <= color.y || 0.0f >= color.y) {directions.y *= -1; color.y = Math.max(0.0f, Math.min(color.y, 1.0f));}
		if(1.0f <= color.z || 0.0f >= color.z) {directions.z *= -1; color.z = Math.max(0.0f, Math.min(color.z, 1.0f));}
		
		uniformColors.get(nextImageIndex).update(logicalDevice, memoryAllocator, color);
		
		descriptorUniformColor.updateDescriptorSet(logicalDevice,
				descriptorUniformColor.getDescriptorSet(nextImageIndex),
				uniformColors.get(nextImageIndex));
	}
	
	private void recreateSwapChainAssets(Renderer renderer) {
		destroySwapChainAssets(renderer);
		createSwapChainAssets(renderer);
	}
}

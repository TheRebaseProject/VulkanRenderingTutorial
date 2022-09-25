package renderer;

import static org.lwjgl.system.Configuration.DEBUG;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

import renderer.vulkan.Instance;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.MemoryAllocator;
import renderer.vulkan.descriptor.DescriptorSetLayout;
import renderer.vulkan.descriptor.DescriptorSetLayoutSingleUniform;
import renderer.vulkan.physicalDevice.PhysicalDevice;
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
	 * private variables
	 */
    private DescriptorSetLayout[] descriptorSetLayouts;
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
		
		instance = new Instance();
		logicalDevice = new LogicalDevice();
		memoryAllocator = new MemoryAllocator();
		physicalDevice = new PhysicalDevice();
		swapChain = new SwapChain();
	}
	
	/*
	 * public methods
	 */
	public void create() {
		instance.create();
		physicalDevice.selectPhysicalDevice(instance.getInstance());
		logicalDevice.create(physicalDevice);
		memoryAllocator.create(instance, physicalDevice, logicalDevice);
		
		createDescriptorSetLayouts();
		
		swapChain.create(physicalDevice, logicalDevice);
	}
	
	public void destroy() {
		swapChain.destroy(logicalDevice);
		
		destroyDescriptorSetLayouts();
		
		memoryAllocator.destroy();
		logicalDevice.destroy();
		//no resources released by a PhysicalDevice
		instance.destroy();
	}
	
	public LogicalDevice getLogicalDevice() {return logicalDevice;}
	public void waitForDeviceIdle() {vkDeviceWaitIdle(logicalDevice.device());}
	
	/*
	 * private methods
	 */
    private void createDescriptorSetLayouts() {
    	for(byte i = 0; i < DESCRIPTOR_SET_LAYOUT_COUNT; i++) {
    		descriptorSetLayouts[i].create(logicalDevice);
    	}
    }
    
    private void destroyDescriptorSetLayouts() {
    	for(byte i = 0; i < DESCRIPTOR_SET_LAYOUT_COUNT; i++) {
    		descriptorSetLayouts[i].destroy(logicalDevice);
    	}
    }
}

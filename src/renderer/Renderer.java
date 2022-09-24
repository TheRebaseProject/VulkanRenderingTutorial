package renderer;

import static org.lwjgl.system.Configuration.DEBUG;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

import renderer.vulkan.Instance;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.MemoryAllocator;
import renderer.vulkan.physicalDevice.PhysicalDevice;

public class Renderer {
	/*
	 * public class constants
	 */
	public static final String[] DEVICE_EXTENSIONS = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};
	
    public static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
    public static final String[] VALIDATION_LAYERS = {"VK_LAYER_KHRONOS_validation"};
    
	/*
	 * private variables
	 */
	private Instance instance;
	private LogicalDevice logicalDevice;
	private MemoryAllocator memoryAllocator;
	private PhysicalDevice physicalDevice;
    
    /*
     * constructors
     */
	public Renderer() {
		instance = new Instance();
		logicalDevice = new LogicalDevice();
		memoryAllocator = new MemoryAllocator();
		physicalDevice = new PhysicalDevice();
	}
	
	/*
	 * public methods
	 */
	public void create() {
		instance.create();
		physicalDevice.selectPhysicalDevice(instance.getInstance());
		logicalDevice.create(physicalDevice);
		memoryAllocator.create(instance, physicalDevice, logicalDevice);
	}
	
	public void destroy() {
		memoryAllocator.destroy();
		logicalDevice.destroy();
		//no resources released by a PhysicalDevice
		instance.destroy();
	}
	
	public void waitForDeviceIdle() {vkDeviceWaitIdle(logicalDevice.device());}
}

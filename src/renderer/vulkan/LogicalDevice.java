package renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;

import renderer.Renderer;
import renderer.vulkan.physicalDevice.PhysicalDevice;
import renderer.vulkan.physicalDevice.QueueFamilyIndices;
import utilities.MemoryUtilities;

public class LogicalDevice {
    /*
     * private variables
     */
	private VkDevice device;
    private VkQueue graphicsQueue;
    private PhysicalDevice physicalDevice;
	private VkQueue presentQueue;
	
	/*
	 * constructors
	 */
	public LogicalDevice() {
		device = null;
		graphicsQueue = null;
		physicalDevice = null;
		presentQueue = null;
	}
	
	/*
	 * public methods
	 */
    public void create(PhysicalDevice physicalDevice) {
        this.physicalDevice = physicalDevice;
        
        try(MemoryStack stack = stackPush()) {
            QueueFamilyIndices indices = physicalDevice.getQueueFamilyIndicies();
            VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack);
            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);
            PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);
            int[] uniqueQueueFamilies = indices.unique();
            VkDeviceQueueCreateInfo.Buffer deviceQueueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.length, stack);

            for(int i = 0; i < uniqueQueueFamilies.length; i++) {
                VkDeviceQueueCreateInfo queueCreateInfo = deviceQueueCreateInfos.get(i);
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
            }

            physicalDeviceFeatures.samplerAnisotropy(true);
            
            deviceCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            deviceCreateInfo.pQueueCreateInfos(deviceQueueCreateInfos);
            deviceCreateInfo.pEnabledFeatures(physicalDeviceFeatures);
            deviceCreateInfo.ppEnabledExtensionNames(MemoryUtilities.asPointerBuffer(Renderer.DEVICE_EXTENSIONS));

            if(Renderer.ENABLE_VALIDATION_LAYERS) {
                deviceCreateInfo.ppEnabledLayerNames(MemoryUtilities.asPointerBuffer(Renderer.VALIDATION_LAYERS));
            }

            if(VK_SUCCESS == vkCreateDevice(physicalDevice.physicalDevice(), deviceCreateInfo, null, pDevice)) {
                device = new VkDevice(pDevice.get(0), physicalDevice.physicalDevice(), deviceCreateInfo);

                vkGetDeviceQueue(device, indices.getGraphicsFamily(), 0, pQueue);
                graphicsQueue = new VkQueue(pQueue.get(0), device);
     
                vkGetDeviceQueue(device, indices.getPresentFamily(), 0, pQueue);
                presentQueue = new VkQueue(pQueue.get(0), device);
            } else {
                throw new RuntimeException("Failed to create logical device");
            }
        }
    }
    
    public void destroy() {vkDestroyDevice(device, null);}
    public VkDevice device() {return device;}
    public VkQueue getGraphicsQueue() {return graphicsQueue;}
    public PhysicalDevice getPhysicalDevice() {return physicalDevice;}
    public VkQueue getPresentQueue() {return presentQueue;}
}

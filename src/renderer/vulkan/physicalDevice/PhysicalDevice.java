package renderer.vulkan.physicalDevice;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import main.WindowManager;
import renderer.Renderer;

public class PhysicalDevice {
    /*
     * private variables
     */
	private VkPhysicalDevice physicalDevice;
	private QueueFamilyIndices queueFamilyIndices;
	
	/*
	 * constructors
	 */
	public PhysicalDevice() {
		physicalDevice = null;
		queueFamilyIndices = null;
	}
	
	/*
	 * public methods
	 */
    public void selectPhysicalDevice(VkInstance instance) {
        try(MemoryStack stack = stackPush()) {
            IntBuffer deviceCount = stack.ints(0);
            PointerBuffer ppPhysicalDevices;
            VkPhysicalDevice physicalDevice = null;

            vkEnumeratePhysicalDevices(instance, deviceCount, null);

            if(0 == deviceCount.get(0)) {throw new RuntimeException("No GPU with Vulkan support found");}

            ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
            vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);

            for(int i = 0; i < ppPhysicalDevices.capacity(); i++) {
                physicalDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);
                
                if(meetsRequirments(physicalDevice)) {
                	break;
                } else {
                	physicalDevice = null;
                }
            }

            if(null != physicalDevice) {
            	this.physicalDevice = physicalDevice;
            } else {
            	throw new RuntimeException("Failed to find a suitable GPU");
            }            
        }
    }

    public QueueFamilyIndices getQueueFamilyIndicies() {return queueFamilyIndices;}
    public VkPhysicalDevice physicalDevice() {return physicalDevice;}
    
    public SwapChainSupportInfo querySwapChainSupport(VkPhysicalDevice physicalDevice, long surface, MemoryStack stack) {
        SwapChainSupportInfo supportInfo = new SwapChainSupportInfo();
        IntBuffer count = stack.ints(0);

        supportInfo.setCapabilities(VkSurfaceCapabilitiesKHR.malloc(stack));
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, supportInfo.getCapabilities());        

        vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, count, null);

        if(0 != count.get(0)) {
            supportInfo.setFormats(VkSurfaceFormatKHR.malloc(count.get(0), stack));
            vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, count, supportInfo.getFormats());
        }

        vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice,surface, count, null);

        if(0 != count.get(0)) {
            supportInfo.setPresentModes(stack.mallocInt(count.get(0)));
            vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, count, supportInfo.getPresentModes());
        }

        return supportInfo;
    }
	
	/*
	 * private methods
	 */    
	private boolean checkDeviceExtensionSupport(VkPhysicalDevice physicalDevice) {
    	boolean supported = false;
    	
        try(MemoryStack stack = stackPush()) {
            IntBuffer extensionCount = stack.ints(0);
            VkExtensionProperties.Buffer availableExtensions;
            supported = true;
            
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String)null, extensionCount, null);
            availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String)null, extensionCount, availableExtensions);

            for(int i = 0; i < Renderer.DEVICE_EXTENSIONS.length; i++) {
            	boolean found = false;
            	
            	for(int j = 0; j < availableExtensions.capacity(); j++) {            		
            		if(0 == Renderer.DEVICE_EXTENSIONS[i].compareTo(availableExtensions.get(j).extensionNameString())) {
            			found = true;
            			break;
            		}
            	}
            	
            	if(!found) {
            		supported = false;
            		break;
            	}
            }
        }
        
        return supported;
    }
	
    private QueueFamilyIndices findQueueFamilies(VkPhysicalDevice physicalDevice, long surface) {
        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush()) {
            IntBuffer queueFamilyCount = stack.ints(0);
            VkQueueFamilyProperties.Buffer queueFamilies;
            IntBuffer presentSupport = stack.ints(VK_FALSE);

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);
            queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, queueFamilies);
            
            for(int i = 0; i < queueFamilies.capacity(); i++) {
            	vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, presentSupport);
            	
                if(0 != (queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT)) {indices.setGraphicsFamily(i);}
                if(VK_TRUE == presentSupport.get(0)) {indices.setPresentFamily(i);}               
                if(indices.isComplete()) {break;}
            }

            return indices;
        }
    }
    
    private boolean meetsRequirments(VkPhysicalDevice physicalDevice) {
    	boolean meetsRequirements = false;
     	long surface = WindowManager.getSurface();
    	QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice, surface);
    	
    	if(checkDeviceExtensionSupport(physicalDevice) && queueFamilyIndices.isComplete()) {
        	try(MemoryStack stack = stackPush()){
                VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.calloc(stack);
                SwapChainSupportInfo supportInfo = querySwapChainSupport(physicalDevice, surface, stack);                
                
                vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);
                
                meetsRequirements = (VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU == physicalDeviceProperties.deviceType()) &&
                		(supportInfo.getFormats().hasRemaining() && supportInfo.getPresentModes().hasRemaining());
        	}	
    	}
        
        if(meetsRequirements) {this.queueFamilyIndices = queueFamilyIndices;}
        
        return meetsRequirements;
    }
}

package renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkLayerProperties;

import renderer.Renderer;

public class ValidationLayer {
	/*
	 * private class variables
	 */
	private static long debugMessenger;
	
    /*
     * public class methods
     */
    public static boolean checkValidationLayerSupport() {
    	boolean supported = false;
    	
        try(MemoryStack stack = stackPush()) {
            IntBuffer layerCount = stack.ints(0);
            VkLayerProperties.Buffer availableLayers;
            supported = true;

            vkEnumerateInstanceLayerProperties(layerCount, null);
            availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);
            vkEnumerateInstanceLayerProperties(layerCount, availableLayers);
            
            for(int i = 0; i < Renderer.VALIDATION_LAYERS.length; i++) {
            	boolean found = false;
            	
            	for(int j = 0; j < availableLayers.capacity(); j++) {            		
            		if(0 == Renderer.VALIDATION_LAYERS[i].compareTo(availableLayers.get(j).layerNameString())) {
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
    
    public static int createDebugUtilsMessengerEXT(VkInstance instance,
    		VkDebugUtilsMessengerCreateInfoEXT createInfo,
            VkAllocationCallbacks allocationCallbacks,
            LongBuffer pDebugMessenger) {
    	int result = VK_ERROR_EXTENSION_NOT_PRESENT;
    	
		if(NULL != vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT")) {
		    result = vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
		}
		
	    return result;
	}
    
    public static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        if(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT <= messageSeverity) {
            System.err.println("Validation layer: " + callbackData.pMessageString());
        }

        return VK_FALSE;
    }
	
	public static void destroyDebugUtilsMessengerEXT(VkInstance instance, VkAllocationCallbacks allocationCallbacks) {
		if(NULL != vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT")) {
		    vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
		}	
	}
	
    public static void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
        debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT |
        		VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
        		VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
        		VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
        		VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        debugCreateInfo.pfnUserCallback(ValidationLayer::debugCallback);
    }
    
    public static void setupDebugMessenger(VkInstance instance) {
        try(MemoryStack stack = stackPush()) {
            LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);
            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);

            ValidationLayer.populateDebugMessengerCreateInfo(createInfo);

            if(VK_SUCCESS != ValidationLayer.createDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger)) {
                throw new RuntimeException("Failed to set up debug messenger");
            }

            debugMessenger = pDebugMessenger.get(0);
        }
    }
}

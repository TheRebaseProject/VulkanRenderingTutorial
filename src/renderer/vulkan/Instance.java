package renderer.vulkan;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import main.WindowManager;
import renderer.Renderer;
import utilities.MemoryUtilities;

public class Instance {
	/*
	 * private class constants
	 */
	private static final String APPLICATION_NAME = "Vulkan Renderer";
	private static final String ENGINE_NAME = "No Engine";
	
	/*
	 * private variables
	 */
	private VkInstance instance;
	
    /*
     * constructors
     */
	public Instance () {
		instance = null;
	}
	
	/*
	 * public methods
	 */	
    public void create() {
        if(Renderer.ENABLE_VALIDATION_LAYERS && !ValidationLayer.checkValidationLayerSupport()) {
            throw new RuntimeException("Validation Layer requested but not supported");
        }
        
        try(MemoryStack stack = stackPush()) {
            VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack);
            VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(stack);
            PointerBuffer pInstance = stack.mallocPointer(1);

            applicationInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            applicationInfo.pApplicationName(stack.UTF8Safe(APPLICATION_NAME));
            applicationInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
            applicationInfo.pEngineName(stack.UTF8Safe(ENGINE_NAME));
            applicationInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
            applicationInfo.apiVersion(VK_MAKE_VERSION(1, 1, 0));

            instanceCreateInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            instanceCreateInfo.pApplicationInfo(applicationInfo);
            instanceCreateInfo.ppEnabledExtensionNames(getRequiredExtensions());
            instanceCreateInfo.ppEnabledLayerNames(null);

            if(Renderer.ENABLE_VALIDATION_LAYERS) {
                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
                
                ValidationLayer.populateDebugMessengerCreateInfo(debugCreateInfo);
                instanceCreateInfo.ppEnabledLayerNames(MemoryUtilities.asPointerBuffer(Renderer.VALIDATION_LAYERS));
                instanceCreateInfo.pNext(debugCreateInfo.address());
            }

            if(VK_SUCCESS == vkCreateInstance(instanceCreateInfo, null, pInstance)) {
                instance = new VkInstance(pInstance.get(0), instanceCreateInfo);
                WindowManager.createSurface(instance);
        		if(Renderer.ENABLE_VALIDATION_LAYERS) {ValidationLayer.setupDebugMessenger(instance);}	
            } else {
                throw new RuntimeException("Failed to create Vulkan instance");
            }
        }
    }
    
    public void destroy() {
        if(Renderer.ENABLE_VALIDATION_LAYERS) {ValidationLayer.destroyDebugUtilsMessengerEXT(instance, null);}
    	WindowManager.destroySurface(instance);
    	vkDestroyInstance(instance, null);
    }
    
    public VkInstance getInstance() {return instance;}
    
    /*
     * private methods
     */
    private PointerBuffer getRequiredExtensions() {    
        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

        if(Renderer.ENABLE_VALIDATION_LAYERS) {
            MemoryStack stack = stackGet();
            PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);

            extensions.put(glfwExtensions);
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

            return extensions.rewind();
        }

        return glfwExtensions;
    }
}

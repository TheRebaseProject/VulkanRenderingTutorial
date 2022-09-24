package main;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;

public class WindowManager {
	/*
	 * public class constants
	 */
	public static final short WIDTH = 640;
	public static final short HEIGHT = 480;
	
	/*
	 * private class constants
	 */
	private static final String TITLE = "Vulkan Renderering Tutorial";
	
	/*
	 * private class variables
	 */
	private static boolean framebufferResized;
	private static long surface;
	private static long window;
	
	/*
	 * public class methods
	 */
    public static void createSurface(VkInstance instance) {
        try(MemoryStack stack = stackPush()) {
            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            if(VK_SUCCESS == glfwCreateWindowSurface(instance, window, null, pSurface)) {
                surface = pSurface.get(0);
            } else {
                throw new RuntimeException("Failed to create window surface");
            }
        }
    }
    
	public static void createWindow() {
		GLFWErrorCallback.createPrint(System.err).set();
		
        if(!glfwInit()) {throw new RuntimeException("GLFW initialization fialed");}

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if(NULL == window) {throw new RuntimeException("GLFW failed to create a window");}
        
		try(MemoryStack stack = stackPush()) {
			IntBuffer height = stack.mallocInt(1);
			IntBuffer width = stack.mallocInt(1);
			glfwGetWindowSize(window, width, height);
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(window,
				(vidmode.width() - width.get(0)) / 2,
				(vidmode.height() - height.get(0)) / 2);
	    }
		
		glfwShowWindow(window);
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);	
	    glfwSetFramebufferSizeCallback(window, WindowManager::framebufferResizeCallback);
	    
	    framebufferResized = false;
	}
	
	public static void destroySurface(VkInstance instance) {vkDestroySurfaceKHR(instance, surface, null);}
	
	public static void destroyWindow() {
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static boolean framebufferResized() {return framebufferResized;}
	public static long getWindow() {return window;}
	public static boolean windowShouldClose() {return glfwWindowShouldClose(window);}
	
	/*
	 * private class methods
	 */
	private static void framebufferResizeCallback(long window, int width, int height) {framebufferResized = true;}
}

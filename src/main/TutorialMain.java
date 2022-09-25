package main;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;

import renderer.Renderer;

public class TutorialMain {
	/*
	 * public class methods
	 */
    public static void main(String[] args) {new TutorialMain().run();}
    
    /*
     * private variables
     */
    private Renderer renderer;
    
    /*
     * constructors
     */
    public TutorialMain() {
    	renderer = new Renderer();
    }
    
    /*
     * private methods
     */
    private void pollInputs() {
    	glfwPollEvents();
    }
    
    private void render() {
    	int vkResult = renderer.acquireNextSwapChainImage();
    	
    	if(VK_ERROR_OUT_OF_DATE_KHR == vkResult) {
			renderer.recreateSwapChain();
			//recreateSwapChainAssets
		}
    	
    	//prepareDrawAssets
    	
    	renderer.beginCommandBuffer();
    	renderer.beginRenderPass();
    	
    	//draw
        
        renderer.endRenderPass();
    	
    	if(renderer.submit()) {
    		//recreateSwapChainAssets
    	}
    }
    
    private void run() {
    	WindowManager.createWindow();
    	renderer.create();
    	
    	while(!WindowManager.windowShouldClose()) {
    		pollInputs();
    		render();
    	}
    	
    	renderer.waitForDeviceIdle();
    	renderer.destroy();
		WindowManager.destroyWindow();
		System.exit(0);
    }
}

package main;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

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
     * public methods
     */
    public void run() {
    	WindowManager.createWindow();
    	renderer.create();
    	
    	while(!WindowManager.windowShouldClose()) {
    		glfwPollEvents();
    	}
    	
    	renderer.waitForDeviceIdle();
    	renderer.destroy();
		WindowManager.destroyWindow();
		System.exit(0);
    }
}

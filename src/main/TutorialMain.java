package main;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class TutorialMain {
	/*
	 * public class methods
	 */
    public static void main(String[] args) {new TutorialMain().run();}
    
    /*
     * constructors
     */
    public TutorialMain() {}
    
    /*
     * public methods
     */
    public void run() {
    	WindowManager.createWindow();
    	
    	while(!WindowManager.windowShouldClose()) {
    		glfwPollEvents();
    	}
    	
		WindowManager.destroyWindow();
		System.exit(0);
    }
}

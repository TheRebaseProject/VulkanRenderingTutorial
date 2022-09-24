package renderer.vulkan.physicalDevice;

import java.util.stream.IntStream;

public class QueueFamilyIndices {
	/*
	 * private variables
	 */
    private Integer graphicsFamily;
    private Integer presentFamily;
    
    /*
     * constructors
     */
    public QueueFamilyIndices() {
    	graphicsFamily = null;
    	presentFamily = null;
    }

    /*
     * private methods
     */
    public Integer getGraphicsFamily() {return graphicsFamily;}
    public Integer getPresentFamily() {return presentFamily;}
    public boolean isComplete() {return null != graphicsFamily && null != presentFamily;}
    public int[] unique() {return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();}
    public void setGraphicsFamily(int graphicsFamily) {this.graphicsFamily = graphicsFamily;}
    public void setPresentFamily(int presentFamily) {this.presentFamily = presentFamily;}
}

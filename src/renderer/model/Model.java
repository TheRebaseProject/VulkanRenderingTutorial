package renderer.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import renderer.Renderer;
import renderer.vulkan.vertexData.VertexData;

public class Model {
	/*
	 * protected variables
	 */
    protected Matrix4f modelMatrix;
	protected Vector3f position;
	protected Vector3f rotation;
	protected Vector3f scale;
	protected VertexData vertexData;	
	
    /*
     * constructors
     */
	public Model() {
		modelMatrix = new Matrix4f();
		position = new Vector3f(0.0f, 0.0f, 0.0f);
		rotation = new Vector3f(0.0f, 0.0f, 0.0f);
		scale = new Vector3f(1.0f, 1.0f, 1.0f);
		vertexData = null;
	}
	
	/*
	 * public methods
	 */
	public void deinit(Renderer renderer) {}
	public VertexData getVertexData() {return vertexData;}
	public void init(Renderer renderer) {}
	
	public Matrix4f prepareModelMatrix() {		
		modelMatrix.identity();		
		modelMatrix.translate(position);
		modelMatrix.scale(scale);
		modelMatrix.rotateXYZ((float)Math.toRadians(rotation.x), (float)Math.toRadians(rotation.y), (float)Math.toRadians(rotation.z));
		
		return modelMatrix;
	}
	
	public void setPosition(Vector3f position) {
		this.position.x = position.x;
		this.position.y = position.y;
		this.position.z = position.z;
	}
	
	public void setRotation(Vector3f rotation) {
		if(null == rotation) {
			this.rotation.x = 0;
			this.rotation.y = 0;
			this.rotation.z = 0;
		} else {
			this.rotation.x = rotation.x;
			this.rotation.y = rotation.y;
			this.rotation.z = rotation.z;
		}
	}
	
	public void setScale(Vector3f scale) {
		this.scale.x = scale.x;
		this.scale.y = scale.y;
		this.scale.z = scale.z;
	}
}

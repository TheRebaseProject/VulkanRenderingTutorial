#version 450

/*
 * inputs
 */
layout(location = 0) in vec2 inputPosition;
layout(location = 1) in vec2 inputTexelCoords;
 
/*
 * functions
 */
void main() {    
    gl_Position = vec4(inputPosition, 0.0, 1.0);
}
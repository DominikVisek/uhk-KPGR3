#version 150
in vec2 inPosition; // input from the vertex buffer
//in vec3 inColor; // input from the vertex buffer
out vec3 vertColor; // output from this shader to the next pipeline stage
uniform float time; // variable constant for all vertices in a single draw

uniform mat4 proj;
uniform mat4 view;

const float PI = 3.14;

// ohnutí gridu do podoby koule
vec3 getSphere(vec2 xy) {
    float az = xy.x * PI;
    float ze = xy.y * PI/2; // máme od -1 do 1 a chceme od -PI/2 do PI/2
    float r = 1;

    float x = cos(az)*cos(ze)*r;
    float y = 2*sin(az)*cos(ze)*r;
    float z = 0.5*sin(ze)*r;
    return vec3(x, y, z);
}

void main() {
    vec2 pos = inPosition * 2 - 1;
	//vec2 position = inPosition;
	//position.x += 0.1;
	//position.y += cos(position.x + time);
	vec3 sphere = getSphere(pos);
    vertColor = sphere;
	gl_Position = proj * view * vec4(sphere, 1.0);
	//vertColor = inColor;
} 

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

vec3 getSphplot(vec2 xy) {
    float rho = 1;
    float phi = xy.x * PI;
    float theta = xy.y * (2 * PI);

    float x = rho * sin(phi) * cos(theta);
    float y = rho * sin(phi) * sin(theta);
    float z = rho * cos(phi);
    return vec3(x, y, z);
}

vec3 getCylin(vec2 xy) {
    float r = 1;
    float theta = xy.y * (2 * PI);
    float z = xy.x * 2;

    float x = r * cos(theta);
    float y = r * sin(theta);
    return vec3(x, y, z);
}

vec3 getParsur(vec2 xy) {
    float s = xy.y * (2 * PI);

    float x = xy.x * cos(s);
    float y = xy.x * sin(s);
    float z = xy.x;

    return vec3(x, y, z);
}

void main() {
    vec2 pos = inPosition * 2 - 1;
    vec2 position = inPosition;

    //	vec3 sphere = getSphere(pos);

    position.y += sin(position.y + time);

    vec3 sphere = getSphplot(pos * position.y);
    //    vec3 sphere = getCylin(pos);
    //    vec3 sphere = getParsur(pos);

    position.x += 0.1;
    position.y += cos(position.y + time);
    vertColor = sphere;

	gl_Position = proj * view * vec4(sphere, 1.0);
	//vertColor = inColor;
} 

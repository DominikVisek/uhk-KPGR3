#version 150
in vec2 inPosition;// input from the vertex buffer

uniform mat4 viewLight;
uniform mat4 projLight;
uniform int mode;
uniform float time;

const float PI = 3.1415;

// ohnutí gridu do podoby elipsoidu
vec3 getSphere(vec2 xy) {
    float az = xy.x * PI;
    float ze = xy.y * PI/2;// máme od -1 do 1 a chceme od -PI/2 do PI/2
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

vec3 getSphplot2(vec2 xy) {
    float s= PI * 0.2 - PI * xy.x * 4 + 12;
    float t= PI * 0.2 - PI * xy.y * 4 + 12;

    float r = 1 + sin(t)/2;

    return vec3(r*cos(s), r*sin(s), t);
}

vec3 getSombrero(vec2 xy) {
    float s = PI * 0.5 - PI * xy.x *2;
    float t = 2 * PI * xy.y;

    return vec3(
    t*cos(s),
    t*sin(s),
    2*sin(t))/2;
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

vec3 getModifiedSphere(vec2 xy) {
    float r = 3/2;
    float s = (PI * 10 - PI * xy.y) - 2;
    float t = 10 * PI * xy.x;

    float x = r * cos(t) * cos(s);
    float y = r * cos(t) * sin(s) * sin(t);
    float z = r * sin(t);

    return vec3(x+2, y+0.5, z);
}

vec3 getWall(vec2 xy) {
    return vec3(xy * 2, -2.0);// posuneme po ose "z" o 1
}

void main() {
    vec2 pos = inPosition * 2 - 1;// máme od 0 do 1 a chceme od -1 do 1 (funkce pro ohyb gridu s tím počítají)
    vec2 position = inPosition * 2 - 1;
    pos.x += cos(pos.x + (time / 2));
    vec3 finalPos;
    if (mode == 0) {
        // mode 0 je stínící plocha
        finalPos = getWall(position);// posuneme po ose "z" o 1
    } else if (mode == 1){
        // mode 1 modified sphere
        finalPos = getModifiedSphere(position);
    } else {
        // mode 2 parsur
        finalPos = getParsur(pos);
//        finalPos = getModifiedSphere(pos);
//        finalPos = getCylin(pos);
//        finalPos = getSombrero(pos);
//        finalPos = getSphplot2(pos);
//        finalPos = getSphere(pos);
    }

    gl_Position = projLight * viewLight * vec4(finalPos, 1.0);
} 

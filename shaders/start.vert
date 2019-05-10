#version 150
in vec2 inPosition;

uniform mat4 projection;
uniform mat4 view;
uniform vec3 lightPosition;
uniform float time;
uniform int mode;
uniform mat4 lightVP;

out vec4 depthTexCoord;
out vec2 texCoord;
out vec3 normal;
out vec3 light;
out vec3 viewDirection;
out vec3 NdotL;

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

vec3 getSphereNormal(vec2 xy) {
    vec3 u = getSphere(xy + vec2(0.001, 0)) - getSphere(xy - vec2(0.001, 0));
    vec3 v = getSphere(xy + vec2(0, 0.001)) - getSphere(xy - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getWall(vec2 xy) {
    return vec3(xy * 2, -2.0); // posuneme po ose "z" o 1
}

vec3 getWallNormal(vec2 xy) {
    vec3 u = getWall(xy + vec2(0.001, 0)) - getWall(xy - vec2(0.001, 0));
    vec3 v = getWall(xy + vec2(0, 0.001)) - getWall(xy - vec2(0, 0.001));
    return cross(u, v);
}

// ___________________________________________________________

vec3 getSphplot(vec2 xy) {
    float rho = 1;
    float phi = xy.x * PI;
    float theta = xy.y * (2 * PI);

    float x = rho * sin(phi) * cos(theta);
    float y = rho * sin(phi) * sin(theta);
    float z = rho * cos(phi);
    return vec3(x, y, z);
}

vec3 getSphplotNormal(vec2 xy) {
    vec3 u = getSphplot(xy + vec2(0.001, 0)) - getSphplot(xy - vec2(0.001, 0));
    vec3 v = getSphplot(xy + vec2(0, 0.001)) - getSphplot(xy - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getSphplot2(vec2 xy) {
    float s= PI * 0.2 - PI * xy.x * 4 + 12;
    float t= PI * 0.2 - PI * xy.y * 4 + 12;

    float r = 1 + sin(t)/2;

    return vec3(r*cos(s), r*sin(s), t);
}

vec3 getSphplot2Normal(vec2 xy) {
    vec3 u = getSphplot2(xy + vec2(0.001, 0)) - getSphplot2(xy - vec2(0.001, 0));
    vec3 v = getSphplot2(xy + vec2(0, 0.001)) - getSphplot2(xy - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getSombrero(vec2 xy) {
    float s = PI * 0.5 - PI * xy.x *2;
    float t = 2 * PI * xy.y;

    return vec3(
    t*cos(s),
    t*sin(s),
    2*sin(t))/2;
}

vec3 getSombreroNormal(vec2 xy) {
    vec3 u = getSombrero(xy + vec2(0.001, 0)) - getSombrero(xy - vec2(0.001, 0));
    vec3 v = getSombrero(xy + vec2(0, 0.001)) - getSombrero(xy - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getCylin(vec2 xy) {
    float r = 1;
    float theta = xy.y * (2 * PI);
    float z = xy.x * 2;

    float x = r * cos(theta);
    float y = r * sin(theta);
    return vec3(x, y, z);
}

vec3 getCylinNormal(vec2 xy) {
    vec3 u = getCylin(xy + vec2(0.001, 0)) - getCylin(xy - vec2(0.001, 0));
    vec3 v = getCylin(xy + vec2(0, 0.001)) - getCylin(xy - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getParsur(vec2 xy) {
    float s = xy.y * (2 * PI);

    float x = xy.x * cos(s);
    float y = xy.x * sin(s);
    float z = xy.x;

    return vec3(x, y, z);
}

vec3 getParsurNormal(vec2 xy) {
    vec3 u = getParsur(xy + vec2(0.001, 0)) - getParsur(xy - vec2(0.001, 0));
    vec3 v = getParsur(xy + vec2(0, 0.001)) - getParsur(xy - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getModifiedSphere(vec2 xy) {
    float r = 3/2;
    float s = (PI * 10 - PI * xy.y * 2) - 1;
    float t = 10 * PI * xy.x;

    float x = r * cos(t) * cos(s);
    float y = r * cos(t) * sin(s);
    float z = r * sin(t);

    return vec3(x+2, y+0.5, z);
}

vec3 getModifiedSphereNormal(vec2 xy) {
    vec3 u = getModifiedSphere(xy + vec2(0.001, 0)) - getModifiedSphere(xy - vec2(0.001, 0));
    vec3 v = getModifiedSphere(xy + vec2(0, 0.001)) - getModifiedSphere(xy - vec2(0, 0.001));
    return cross(u, v);
}

void main() {
    vec2 pos = inPosition * 2 - 1;
    vec2 position = inPosition * 2 - 1;
    vec3 finalPos;

    // Alespoň jednu z funkcí modifikujte v čase pomocí uniform proměnné.
    pos.x += cos(pos.x + (time / 2));

    if (mode == 0) {
        finalPos = getWall(position);
        normal = getWallNormal(position);
    } else if (mode == 1){
        finalPos = getModifiedSphere(position);
        normal = getModifiedSphereNormal(position);
    } else {
        // parsur tvar
        finalPos = getParsur(pos);
        normal = getParsurNormal(pos);

        // další tvary + tvar ze cvičení

//        finalPos = getModifiedSphere(pos);
//        normal = getModifiedSphereNormal(pos);
//
//        finalPos = getSphere(pos);
//        normal = getSphereNormal(pos);
//
//        finalPos = getModifiedSphere(pos);
//        normal = getModifiedSphereNormal(pos);
//
//        finalPos = getModifiedSphere(pos);
//        normal = getModifiedSphereNormal(pos);
//
//        finalPos = getSphlot(pos);
//        normal = getSphlotNormal(pos);
//
//        finalPos = getSphlot2(pos);
//        normal = getSphlotNormal2(pos);
    }

    gl_Position = projection * view * vec4(finalPos, 1.0);
    light = lightPosition - finalPos;
    NdotL = vec3(dot(normal, light));

    mat4 invView = inverse(view);
    vec3 eyePosition = vec3(invView[3][0], invView[3][1], invView[3][2]);

    viewDirection = eyePosition - finalPos;

    texCoord = inPosition;

    depthTexCoord = lightVP * vec4(finalPos, 1.0);
    depthTexCoord.xyz = (depthTexCoord.xyz + 1) / 2; // obrazovka má rozsahy <-1;1>
}
#version 150
in vec2 inPosition;

uniform mat4 projection;
uniform mat4 view;
uniform float time;
uniform int mode;

out vec4 depthTexCoord;
out vec2 texCoord;
out vec3 normal;

const float PI = 3.14;

// ohnutí gridu do podoby koule

vec3 getSphere(vec2 xy) {
    float az = xy.x * PI;
    float ze = xy.y * PI/2;// máme od -1 do 1 a chceme od -PI/2 do PI/2
    float r = 1;

    float x = cos(az)*cos(ze)*r;
    float y = sin(az)*cos(ze)*r;
    float z = sin(ze)*r;

    return vec3(x, y, z)*2;
}

vec3 getSphereNormal(vec2 xy) {
    vec3 u = getSphere(xy + vec2(0.001, 0)) - getSphere(xy - vec2(0.001, 0));
    vec3 v = getSphere(xy + vec2(0, 0.001)) - getSphere(xy - vec2(0, 0.001));
    return cross(u, v);
}

// ___________________________________________________________


void main() {
    // 2x pozice, protože pos je modifikována v čase
    vec2 pos = inPosition * 2 - 1;
    vec3 finalPos;

    // Alespoň jednu z funkcí modifikujte v čase pomocí uniform proměnné.
    pos.x += cos(pos.x + (time / 2));

    finalPos = getSphere(pos);
    normal = getSphereNormal(pos);

    gl_Position = projection * view * vec4(finalPos, 1.0);

    mat4 invView = inverse(view);

    texCoord = inPosition;

    depthTexCoord = vec4(finalPos, 1.0);
    depthTexCoord.xyz = (depthTexCoord.xyz + 1) / 2;// obrazovka má rozsahy <-1;1>
}
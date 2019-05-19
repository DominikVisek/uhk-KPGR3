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
    return vec3(x, y, z)*1.7;
}

vec3 getSphereNormal(vec2 xy) {
    vec3 u = getSphere(xy + vec2(0.001, 0)) - getSphere(xy - vec2(0.001, 0));
    vec3 v = getSphere(xy + vec2(0, 0.001)) - getSphere(xy - vec2(0, 0.001));
    return cross(u, v);
}

void main() {
    vec2 pos = inPosition * 2 - 1;
    vec3 finalPos;

    finalPos = getSphere(pos);
    normal = getSphereNormal(pos);

    gl_Position = projection * view * vec4(finalPos, 1.0);

    light = lightPosition - finalPos;
    NdotL = vec3(dot(normal, light));

    // získání pozice kamery z view matice
    // (kamera je pohled třetí osoby a tudíž její pozice je v počátku - proto nutné použít view matici)
    mat4 invView = inverse(view);
    vec3 eyePosition = vec3(invView[3][0], invView[3][1], invView[3][2]);

    viewDirection = eyePosition - finalPos;

    texCoord = inPosition;

    depthTexCoord = lightVP * vec4(finalPos, 1.0);
    depthTexCoord.xyz = (depthTexCoord.xyz + 1) / 2; // obrazovka má rozsahy <-1;1>
}

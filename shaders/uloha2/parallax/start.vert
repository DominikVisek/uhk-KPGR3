#version 150
in vec2 inParamPos;

out vec3 vertCoor;
out vec3 vertNormal;
out vec3 eyeVec;
out vec3 lightVec;
out vec3 spotDir;

uniform mat4 mat;
uniform vec3 eyePos;
uniform vec3 lightDir;
uniform vec3 lightPos;

const float PI = 3.1415926536;

vec3 getSphere(vec2 paramPos) {
    float a = 2 * PI * paramPos.x;
    float t = PI * paramPos.y;

    float x = cos(a) * sin(t);
    float y = 2*sin(a) * sin(t);
    float z = 0.5 * cos(t);

    return vec3(x, y, z);
}

vec3 getSphereNormal(vec2 paramPos) {
    float d = 1e-5;
    vec2 dx = vec2(d, 0);
    vec2 dy = vec2(0, d);
    vec3 tx = (getSphere(paramPos + dx) - getSphere(paramPos - dx)) / (2 * d);
    vec3 ty = (getSphere(paramPos + dy) - getSphere(paramPos - dy)) / (2 * d);
    return cross(ty, tx);
}

mat3 tangentMat(vec2 paramPos) {
    float d = 1e-5;
    vec2 dx = vec2(d, 0);
    vec2 dy = vec2(0, d);
    vec3 tx = (getSphere(paramPos + dx) - getSphere(paramPos - dx)) / (2 * d);
    vec3 ty = (getSphere(paramPos + dy) - getSphere(paramPos - dy)) / (2 * d);
    vec3 x = normalize(tx);
    vec3 y = normalize(-ty);
    vec3 z = cross(x, y);
    x = cross(y, z);
    return mat3(x,y,z);
}

void main() {
    vec3 vertPosition = getSphere(inParamPos);
    gl_Position = mat * vec4(vertPosition, 1.0);

    vertNormal = getSphereNormal(inParamPos);

    mat3 tanMat = tangentMat(inParamPos);
    eyeVec = (eyePos - vertPosition) * tanMat;
    lightVec = (lightPos - vertPosition) * tanMat;
    spotDir = (lightDir - vertPosition) * tanMat;

    vertCoor = vec3(inParamPos,0);
}

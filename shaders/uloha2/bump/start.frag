#version 150

in vec2 texCoord;
in vec3 normal;

uniform sampler2D textureID;
uniform sampler2D depthTexture;

out vec4 outColor;

mat4 rotationMatrix(vec3 axis, float angle){
    axis = normalize(axis);
    float s = angle;
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,
    oc * axis.x * axis.y - axis.z * s, 1.0, 0.0,
    oc * axis.x * axis.y + axis.z * s,
    oc * axis.y * axis.y + c, oc * axis.y * axis.z - axis.x * s, 0.0,
    oc * axis.z * axis.x - axis.y * s, oc * axis.y * axis.z + axis.x * s,
    oc * axis.z * axis.z + c, 1.0, 1.0, 1.0, 1.0, 1.0);
}

vec3 bump2(vec2 xy, vec3 normal){
    float BumpDensity = 80; float BumpSize = 0.2;
    vec2 p = fract(BumpDensity*xy) - vec2(0.5);
    float d = p.x*p.x + p.y*p.y;

    if (d >= BumpSize){
        return normal;
    }

    return normal * mat3(rotationMatrix(vec3(p.y, - p.x, 0.), -d/BumpSize));
}

void main() {
    vec4 texColor = texture(textureID, texCoord);

    outColor = texColor*vec4(bump2(texCoord, normalize(normal)),0)/1.4;
}

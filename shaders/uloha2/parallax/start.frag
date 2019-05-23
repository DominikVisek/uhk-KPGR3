#version 150
in vec3 vertCoor;
in vec3 vertNormal;
in vec3 eyeVec;
in vec3 lightVec;
in vec3 spotDir;

uniform sampler2D diffTex;
uniform sampler2D normTex;
uniform sampler2D heightTex;
uniform float lightCutoff;
uniform float lightDist;

out vec4 outColor;

void main() {
    vec3 matSpecCol = vec3(1);
    vec3 ambientLightCol = vec3(0.1);
    vec3 directLightCol = vec3(1, 0.9, 0.9);

    float scaleL = 0.04;
    float scaleK = -0.02;

    vec2 texCoord = vertCoor.xy * vec2(1, -1) + vec2(0, 1);

    float height = texture(heightTex, texCoord).r;
    float v = height * scaleL + scaleK;

    vec3 eye = normalize(eyeVec);
    vec2 offset = eye.xy * v;

    texCoord = texCoord + offset;

    vec3 inNormal = texture(normTex, texCoord).xyz * 2 - 1;

    vec3 matDifCol = texture(diffTex, texCoord).xyz * vec3(0.8);
    vec3 lVec = normalize(lightVec);

    vec3 ambiComponent = ambientLightCol * matDifCol;

    float difCoef = pow(max(0, lVec.z), 0.7) * max(0, dot(inNormal, lVec));
    vec3 difComponent = directLightCol * matDifCol * difCoef;

    vec3 reflected = reflect(-lVec, inNormal);

    float specCoef = pow(max(0, lVec.z), 0.7) * pow(max(0, dot(normalize(eyeVec), reflected)), 70);
    vec3 specComponent = directLightCol * matSpecCol * specCoef;

    float spotEffect = max(dot(normalize(spotDir), -lVec), 0);
    float dist = 1 - 1 / (lightDist / length(lightVec));

    if (spotEffect >  lightCutoff) {
        float blend = clamp((spotEffect -  lightCutoff) / (1 - lightCutoff), 0, 1);
        outColor.rgb = mix(ambiComponent, ambiComponent + max(dist * (difComponent + specComponent), 0), blend);
    } else {
        outColor = vec4(ambiComponent, 1);
    }
}

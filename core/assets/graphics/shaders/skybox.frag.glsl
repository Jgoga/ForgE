uniform samplerCube u_skyboxCubemap;
varying vec3        v_texCoord;
const float lowerLimit = 0.0f;
const float upperLimit = 40.0f;

void main() {
  float factor      = (v_texCoord.y - lowerLimit) / (upperLimit - lowerLimit);
  factor            = clamp(factor, 0.0f, 1.0f);

  vec4 finalColor   = textureCube(u_skyboxCubemap, v_texCoord);
  gl_FragColor      = mix(u_skyColor, finalColor, factor);
}

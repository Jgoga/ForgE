struct ShadowMap {
  sampler2D farDepthMap;
  sampler2D nearDepthMap;
  mat4 farTransform;
  mat4 nearTransform;
  vec4 lightPosition;
  float far;
  float near;
};

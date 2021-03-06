package macbury.forge.shaders.uniforms;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import macbury.forge.shaders.utils.BaseRenderableMaterialUniform;

/**
 * Created by macbury on 29.04.15.
 */
public class UniformColorDiffuse extends BaseRenderableMaterialUniform<ColorAttribute> {
  private final static String UNIFORM_COLOR_DIFFUSE = "u_colorDiffuse";

  @Override
  public void dispose() {

  }


  @Override
  public void bindAttribute(ShaderProgram shader, RenderContext context, ColorAttribute attribute) {
    shader.setUniformf(UNIFORM_COLOR_DIFFUSE, attribute.color);
  }

  @Override
  public long getAttributeType() {
    return ColorAttribute.Diffuse;
  }

  @Override
  public void defineUniforms() {
    define(UNIFORM_COLOR_DIFFUSE, Color.class);
  }
}

package macbury.forge.graphics.mesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by macbury on 04.08.15.
 */
public class MeshFactory implements Disposable {
  private final VertexAttribute[] attributes;
  private float verties[];
  private short indices[];

  public MeshFactory(float[] verties, short[] indices, VertexAttribute[] meshAttributtes) {
    this.verties = verties;
    this.indices = indices;
    this.attributes = meshAttributtes;
  }

  public Mesh get() {
    Mesh mesh = new Mesh(true, verties.length, indices.length, attributes);
    mesh.setVertices(verties);
    mesh.setIndices(indices);
    mesh.setAutoBind(false);
    return mesh;
  }

  @Override
  public void dispose() {
    verties = null;
    indices = null;
  }
}

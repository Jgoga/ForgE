package macbury.forge.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import macbury.forge.voxel.ChunkMap;
import macbury.forge.octree.OctreeNode;
import macbury.forge.octree.OctreeObject;

/**
 * Created by macbury on 19.10.14.
 */
public class PositionComponent extends BaseComponent implements OctreeObject {
  public final Vector3    vector;
  public final Quaternion  rotation;
  public final Vector3     size;
  public final Vector3     scale;
  public OctreeNode        parent;
  public final Matrix4     worldTransform;
  public boolean dirty   = true;
  private final static Vector3 temp = new Vector3();
  public Entity entity;

  public PositionComponent() {
    super();
    this.vector         = new Vector3();
    this.rotation       = new Quaternion();
    this.size           = new Vector3();
    this.scale          = new Vector3();
    this.worldTransform = new Matrix4();
  }

  public void setVector(Vector3 in) {
    vector.set(in);
    dirty = true;
  }

  @Override
  public void set(BaseComponent otherComponent) {
    reset();
    PositionComponent otherPosition = (PositionComponent)otherComponent;
    vector.set(otherPosition.vector);
    rotation.set(otherPosition.rotation);
    worldTransform.set(otherPosition.worldTransform);
    size.set(otherPosition.size);
    scale.set(otherPosition.scale);

    dirty = true;
  }

  @Override
  public void reset() {
    entity = null;
    vector.setZero();
    worldTransform.idt();
    rotation.set(Vector3.Z, 90);
    size.set(ChunkMap.TERRAIN_TILE_SIZE);
    scale.set(1,1,1);
    parent = null;
    dirty = true;
  }

  @Override
  public void getBoundingBox(BoundingBox outBox) {
    outBox.set(vector, temp.set(vector).add(size));
  }


  @Override
  public void setOctreeParent(OctreeNode parent) {
    this.parent = parent;
  }

  public Matrix4 updateTransformMatrix() {
    if (dirty) {
      worldTransform.idt();
      worldTransform.setToTranslationAndScaling(vector, scale);
      worldTransform.rotate(rotation);
    }

    return worldTransform;
  }

  public void getBulletMatrix(Matrix4 out) {
    out.idt();
    out.translate(vector);
    out.rotate(rotation);
  }

  public void setFromBulletMatrix(Matrix4 in) {
    in.getTranslation(vector);
    in.getRotation(rotation);
    dirty = true;
  }

  public void applyWorldTransform(Matrix4 out) {
    if (dirty) {
      updateTransformMatrix();
      out.idt();
      out.translate(vector);
      //out.scl()
      out.rotate(rotation);
    }

  }
}

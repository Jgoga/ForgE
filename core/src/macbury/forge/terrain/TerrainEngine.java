package macbury.forge.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import macbury.forge.graphics.batch.renderable.BaseRenderable;
import macbury.forge.graphics.batch.renderable.BaseRenderableProvider;
import macbury.forge.graphics.batch.renderable.VoxelFaceRenderable;
import macbury.forge.graphics.builders.Chunk;
import macbury.forge.graphics.builders.TerrainBuilder;
import macbury.forge.graphics.camera.GameCamera;
import macbury.forge.level.Level;
import macbury.forge.level.map.ChunkMap;
import macbury.forge.octree.OctreeNode;
import macbury.forge.octree.OctreeObject;
import macbury.forge.utils.ActionTimer;

/**
 * Created by macbury on 23.10.14.
 */
public class TerrainEngine implements Disposable, ActionTimer.TimerListener, BaseRenderableProvider {
  private static final float UPDATE_EVERY    = 0.05f;
  private static final String TAG = "TerrainEngine";
  private final ActionTimer       timer;
  private final ChunkMap          map;
  private final OctreeNode        octree;
  private final GameCamera        camera;
  private final TerrainBuilder    builder;
  public  final Array<Chunk>      chunks;
  public  final Array<VoxelFaceRenderable> visibleFaces;
  public  final Array<OctreeObject> tempObjects;
  public  final Vector3 tempA;


  public TerrainEngine(Level level) {
    this.timer = new ActionTimer(UPDATE_EVERY, this);
    this.timer.start();

    this.tempObjects          = new Array<OctreeObject>();
    this.visibleFaces         = new Array<VoxelFaceRenderable>();
    this.chunks               = new Array<Chunk>();
    this.map                  = level.terrainMap;
    this.octree               = level.staticOctree;
    this.camera               = level.camera;
    this.builder              = new TerrainBuilder(map);
    this.tempA                = new Vector3();
  }

  public void update() {
    timer.update(Gdx.graphics.getDeltaTime());
  }

  @Override
  public void onTimerTick(ActionTimer timer) {
    rebuild();
    occulsion();
  }

  private void occulsion() {
    visibleFaces.clear();
    tempObjects.clear();
    octree.retrieve(tempObjects, camera.normalOrDebugFrustrum(), false);

    while(tempObjects.size > 0) {
      Chunk visibleChunk = (Chunk) tempObjects.pop();

      if (visibleChunk.renderables.size > 0) {
        for (int i = 0; i < visibleChunk.renderables.size; i++) {
          VoxelFaceRenderable renderable = visibleChunk.renderables.get(i);

          if (tempA.set(camera.normalOrDebugDirection()).dot(renderable.direction) < 0.0f) {
            visibleFaces.add(renderable);
          }
        }
      }
    }
  }

  /**
   * Rebuild pending chunks in queue, return true if everything has been rebuilded
   * @return
   */
  private boolean rebuild() {
    if (map.chunkToRebuild.size > 0) {
      Gdx.app.log(TAG, "Chunks to rebuild: " + map.chunkToRebuild.size);
      while (map.chunkToRebuild.size > 0) {
        Chunk chunk = map.chunkToRebuild.pop();
        buildChunkGeometry(chunk);
      }

      octree.clear();
      for (int i = 0; i < chunks.size; i++) {
        octree.insert(chunks.get(i));
      }
    }

    return map.chunkToRebuild.size == 0;
  }

  private void buildChunkGeometry(Chunk chunk) {
    chunk.clearFaces();

    builder.begin(); {
      builder.cursor.set(chunk);
      VoxelFaceRenderable renderable = null;

      builder.backFace();
      if (builder.haveGeometry()) {
        renderable = builder.getRenderable();
        renderable.direction.set(0,0, -1);
        chunk.renderables.add(renderable);
      }

      builder.frontFace();
      if (builder.haveGeometry()) {
        renderable = builder.getRenderable();
        renderable.direction.set(0,0, 1);
        chunk.renderables.add(renderable);
      }

      builder.topFace();
      if (builder.haveGeometry()) {
        renderable = builder.getRenderable();
        renderable.direction.set(0,1, 0);
        chunk.renderables.add(renderable);
      }

      builder.bottomFace();
      if (builder.haveGeometry()) {
        renderable = builder.getRenderable();
        renderable.direction.set(0,-1, 0);
        chunk.renderables.add(renderable);
      }

      builder.leftFace();
      if (builder.haveGeometry()) {
        renderable = builder.getRenderable();
        renderable.direction.set(-1,0, 0);
        chunk.renderables.add(renderable);
      }

      builder.rightFace();
      if (builder.haveGeometry()) {
        renderable = builder.getRenderable();
        renderable.direction.set(1,0, 0);
        chunk.renderables.add(renderable);
      }
    } builder.end();

    if (chunk.isEmpty()) {
      remove(chunk);
    } else {
      chunk.updateBoundingBox();

      if (!chunks.contains(chunk, true)) {
        chunks.add(chunk);
      }
    }

  }

  private void remove(Chunk chunk) {
    chunks.removeValue(chunk, true);
    chunk.dispose();
  }

  @Override
  public void dispose() {
    while(chunks.size > 0) {
      remove(chunks.pop());
    }
    builder.dispose();
  }

  @Override
  public void getRenderables(Array<BaseRenderable> renderables) {
    renderables.addAll(visibleFaces);
  }
}
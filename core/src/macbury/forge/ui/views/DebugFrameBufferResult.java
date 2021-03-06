package macbury.forge.ui.views;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import macbury.forge.ForgE;

/**
 * Created by macbury on 20.07.15.
 */
public class DebugFrameBufferResult extends Actor {

  private final TextureRegion region;
  private final String fbid;

  public DebugFrameBufferResult(String fbid) {
    region = new TextureRegion();
    setWidth(Gdx.graphics.getWidth());
    setHeight(Gdx.graphics.getHeight());
    this.fbid = fbid;
    setZIndex(1);
  }



  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    FrameBuffer frameBuffer = ForgE.fb.get(fbid);
    region.setTexture(frameBuffer.getColorBufferTexture());
    region.setRegion(0, 0, frameBuffer.getColorBufferTexture().getWidth(), frameBuffer.getColorBufferTexture().getHeight());
    region.flip(false, true);

    batch.draw(region, getX(), getY(), getWidth(), getHeight());
  }

  public static DebugFrameBufferResult build(String name, int size, float x, float y) {
    DebugFrameBufferResult colorResult = new DebugFrameBufferResult(name);
    colorResult.setWidth(size);
    colorResult.setHeight(size);
    colorResult.setX(x);
    colorResult.setY(y);
    return colorResult;
  }
}

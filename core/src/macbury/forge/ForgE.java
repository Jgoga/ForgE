package macbury.forge;

import com.badlogic.gdx.Game;
import macbury.forge.assets.AssetsManager;
import macbury.forge.graphics.GraphicsUtils;
import macbury.forge.screens.ScreenManager;
import macbury.forge.shaders.ShadersManager;

public class ForgE extends Game {
  public static GraphicsUtils graphics;
  public static ScreenManager screens;
  public static AssetsManager assets;
  public static ShadersManager shaders;
  private ForgEBootListener bootListener;


  @Override
  public void create () {
    graphics = new GraphicsUtils();
    screens  = new ScreenManager(this);
    assets   = new AssetsManager();
    shaders  = new ShadersManager();
    if (bootListener != null) {
      bootListener.afterEngineCreate(this);
    }
  }

  public void setBootListener(ForgEBootListener bootListener) {
    this.bootListener = bootListener;
  }
}

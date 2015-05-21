package macbury.forge.shaders.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import macbury.forge.promises.GdxFutureTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by macbury on 16.10.14.
 */
public class ShadersManager {
  public static final String SHADERS_PATH =  "graphics/shaders/";
  public static final String SHADER_HELPERS_PATH = SHADERS_PATH + "helpers/";
  private static final String TAG = "ShadersManager";
  private Array<ShaderReloadListener> shaderReloadListeners;
  private HashMap<String, BaseShader> shaders;
  private final Array<BaseShader> shaderList;

  public ShadersManager() {
    this.shaderReloadListeners = new Array<ShaderReloadListener>();
    this.shaders               = new HashMap<String, BaseShader>();
    this.shaderList            = new Array<BaseShader>();
    reload();
  }

  public BaseShader get(String name) {
    return shaders.get(name);
  }

  public void reload() {
    Json json = new Json();
    for(String key : shaders.keySet()) {
      shaders.get(key).dispose();
    }
    shaders.clear();
    shaderList.clear();
    FileHandle[] shadersToImport = Gdx.files.internal(SHADERS_PATH).list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.contains(".json");
      }
    });
    for (FileHandle file : shadersToImport) {

      String shaderName     = file.nameWithoutExtension();
      BaseShader shader     = null;
      if (shaders.containsKey(shaderName)) {
        Gdx.app.log(TAG, "Reloading: "+ shaderName);
        shader = shaders.get(shaderName);
      } else {
        Gdx.app.log(TAG, "Loading: "+ shaderName);
        shader   = json.fromJson(BaseShader.class, file);
      }

      if (shader.load(this)) {
        shaders.put(file.nameWithoutExtension(), shader);
        if (!file.nameWithoutExtension().contains("-preview") && !file.nameWithoutExtension().contains("-debug")) {
          shaderList.add(shader);
        } else {
          Gdx.app.log(TAG, "Skipping preview and debug shader");
        }

      } else {
        triggerOnShaderError(shader);
        Gdx.app.log(TAG, shader.getLog());
        throw new GdxRuntimeException("Shader compilation error!");
      }
    }

    triggerOnShaderReload();
  }

  public void addOnShaderReloadListener(ShaderReloadListener listener) {
    if (shaderReloadListeners.indexOf(listener, true) == -1) {
      shaderReloadListeners.add(listener);
    }
  }

  public void removeOnShaderReloadListener(ShaderReloadListener listener) {
    if (shaderReloadListeners.indexOf(listener, true) != -1) {
      shaderReloadListeners.removeValue(listener, true);
    }
  }

  private void triggerOnShaderReload() {
    for (ShaderReloadListener listener : this.shaderReloadListeners) {
      listener.onShadersReload(this);
    }
  }

  private void triggerOnShaderError(BaseShader program) {
    for (ShaderReloadListener listener : this.shaderReloadListeners) {
      listener.onShaderError(this, program);
    }
  }

  public Array<BaseShader> all() {
    return shaderList;
  }
}

package macbury.forge.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import macbury.forge.storage.StorageManager;
import macbury.forge.storage.serializers.FullLevelStateSerializer;
import macbury.forge.storage.serializers.LevelStateBasicInfoSerializer;

import java.io.*;
import java.util.HashMap;

/**
 * Created by macbury on 09.03.15.
 */
public class LevelManager {
  private static final String TAG = "LevelManager";
  private final StorageManager storageManager;
  private final LevelStateBasicInfoSerializer basicLevelInfoSerializer;
  private HashMap<Integer, FileHandle> idToPathMap;

  public FileFilter mapAndDirFileFilter = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(LevelState.FILE_EXT) || pathname.isDirectory();
    }
  };

  public LevelManager(StorageManager storageManager) {
    this.storageManager = storageManager;
    this.idToPathMap    = new HashMap<Integer, FileHandle>();
    this.basicLevelInfoSerializer = new LevelStateBasicInfoSerializer();
    reload();
  }

  public LevelState load(FileHandle mapFile) {
    Kryo kryo             = storageManager.pool.borrow();
    LevelState levelState = null;
    Gdx.app.log(TAG, "Loading map: " + mapFile.toString());
    try {
      Input input = new Input(new FileInputStream(mapFile.file()));
      levelState = kryo.readObject(input, LevelState.class, new FullLevelStateSerializer());
      input.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    storageManager.pool.release(kryo);
    return levelState;
  }

  public void save(LevelState state) {
    Kryo kryo          = storageManager.pool.borrow();
    FileHandle mapFile = Gdx.files.internal(LevelState.MAP_STORAGE_DIR+LevelState.MAP_NAME_PREFIX+state.getId()+LevelState.FILE_EXT);
    if (mapFile.exists()) {
      mapFile.file().delete();
    }
    Gdx.app.log(TAG, "Saving map: " + mapFile.toString());
    try {
      synchronized (state) {
        Output output = new Output(new FileOutputStream(mapFile.file(), false));
        kryo.writeObject(output, state, new FullLevelStateSerializer());
        output.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    storageManager.pool.release(kryo);
  }

  private void getHandles(FileHandle begin, Array<FileHandle> handles)  {
    FileHandle[] newHandles = begin.list(mapAndDirFileFilter);
    for (FileHandle f : newHandles) {
      if (f.isDirectory()) {
        getHandles(f, handles);
      } else {
        handles.add(f);
      }
    }
  }

  public void reload() {
    Kryo kryo                                = storageManager.pool.borrow();
    Array<FileHandle> tempFiles              = new Array<FileHandle>();
    getHandles(Gdx.files.internal(LevelState.MAP_STORAGE_DIR) ,tempFiles);
    for (FileHandle file : tempFiles) {
      if (!file.isDirectory()) {
        int levelId = getLevelId(file);
        idToPathMap.put(levelId, file);
      }
    }
    storageManager.pool.release(kryo);
  }

  public LevelState loadBasicLevelStateInfo(int levelId) {
    LevelState levelState = null;
    Kryo kryo             = storageManager.pool.borrow();
    FileHandle mapFile    = getLevelFileHandle(levelId);
    Gdx.app.log(TAG, "Loading map: " + mapFile.toString());
    try {
      Input input = new Input(new FileInputStream(mapFile.file()));
      levelState  = kryo.readObject(input, LevelState.class, basicLevelInfoSerializer);
      input.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    storageManager.pool.release(kryo);
    return levelState;
  }

  private FileHandle getLevelFileHandle(int levelId) {
    return idToPathMap.get(levelId);
  }

  public int getLevelId(FileHandle file) {
    return Integer.valueOf(file.nameWithoutExtension().replaceAll(LevelState.MAP_NAME_PREFIX, ""));
  }


  public FileHandle getFileHandle(int id) {
    return idToPathMap.get(id);
  }
}
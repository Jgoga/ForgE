package macbury.forge.editor.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import macbury.forge.ForgE;
import macbury.forge.editor.controllers.listeners.OnMapChangeListener;
import macbury.forge.editor.parell.Job;
import macbury.forge.editor.parell.JobListener;
import macbury.forge.editor.parell.JobManager;
import macbury.forge.editor.parell.jobs.LoadLevelJob;
import macbury.forge.editor.parell.jobs.NewLevelJob;
import macbury.forge.editor.parell.jobs.SaveLevelGeometryJob;
import macbury.forge.editor.parell.jobs.SaveLevelStateJob;
import macbury.forge.editor.runnables.UpdateStatusBar;
import macbury.forge.editor.screens.LevelEditorScreen;
import macbury.forge.editor.windows.MainWindow;
import macbury.forge.editor.windows.MapCreationWindow;
import macbury.forge.editor.windows.ProgressTaskDialog;
import macbury.forge.level.LevelState;
import macbury.forge.shaders.utils.BaseShader;
import macbury.forge.shaders.utils.ShaderReloadListener;
import macbury.forge.shaders.utils.ShadersManager;
import macbury.forge.time.TimeManager;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Created by macbury on 18.10.14.
 */
public class ProjectController implements JobListener, ShaderReloadListener, MapCreationWindow.Listener, LevelEditorScreen.ForgeAfterRenderListener {
  private static final String LEVEL_STATE_LOADED_CALLBACK = "onLevelStateLoaded";
  private static final String TAG = "ProjectController";
  private static final String LEVEL_STATE_SAVE_CALLBACK = "onLevelStateSaved";
  private MainWindow mainWindow;
  public LevelEditorScreen levelEditorScreen;
  private Array<OnMapChangeListener> onMapChangeListenerArray = new Array<OnMapChangeListener>();
  public JobManager jobs;
  private ProgressTaskDialog progressTaskDialog;
  private JProgressBar jobProgressBar;
  private LevelState currentLevelState;
  private UpdateStatusBar updateStatusBar;

  public void setMainWindow(final MainWindow mainWindow) {
    this.mainWindow         = mainWindow;
    this.jobs               = mainWindow.jobs;

    this.progressTaskDialog = mainWindow.progressTaskDialog;
    this.jobProgressBar     = mainWindow.jobProgressBar;
    jobProgressBar.setVisible(false);

    ForgE.shaders.addOnShaderReloadListener(this);
    this.jobs.addListener(this);

    mainWindow.addWindowListener(new WindowListener() {
      @Override
      public void windowOpened(WindowEvent e) {

      }

      @Override
      public void windowClosing(WindowEvent e) {
        if (closeAndSaveChangesMap()) {
          jobs.waitForAllToComplete();
          mainWindow.dispose();
          System.exit(0);
        }
      }

      @Override
      public void windowClosed(WindowEvent e) {

      }

      @Override
      public void windowIconified(WindowEvent e) {

      }

      @Override
      public void windowDeiconified(WindowEvent e) {

      }

      @Override
      public void windowActivated(WindowEvent e) {

      }

      @Override
      public void windowDeactivated(WindowEvent e) {

      }
    });

    updateUI();
  }

  private void updateUI() {
    boolean editorScreenEnabled = levelEditorScreen != null;
    //TODO: change visibility of main ui
    mainWindow.mainSplitPane.setVisible(true);
    mainWindow.openGlContainer.setVisible(editorScreenEnabled);
    //mainWindow.toolsPane.setVisible(editorScreenEnabled);
  }

  public MainWindow getMainWindow() {
    return mainWindow;
  }

  public void newMap(String storeDir) {
    if (closeAndSaveChangesMap()) {
      LevelState newMapState                 = new LevelState(ForgE.db);
      MapCreationWindow.MapDocument document = new MapCreationWindow.MapDocument(newMapState, storeDir);
      MapCreationWindow newMapWindow         = new MapCreationWindow(document, this);
      newMapWindow.show(mainWindow);
    }
  }

  public void newMap() {
    newMap(Gdx.files.internal(LevelState.MAP_STORAGE_DIR).file().getAbsolutePath());
  }

  @Override
  public void onMapCreationSuccess(MapCreationWindow window, MapCreationWindow.MapDocument document) {
    NewLevelJob job = new NewLevelJob(document.state, document.storeDir);
    job.setCallback(this, LEVEL_STATE_LOADED_CALLBACK);
    jobs.enqueue(job);
  }

  public boolean openMap(FileHandle fileHandle) {
    if (closeAndSaveChangesMap()) {
      LoadLevelJob job = new LoadLevelJob(fileHandle);
      job.setCallback(this, LEVEL_STATE_LOADED_CALLBACK);
      jobs.enqueue(job);
      return true;
    } else {
      return false;
    }
  }

  public void saveMap() {
    if (levelEditorScreen.changeManager.haveChanges()) {
      SaveLevelStateJob job = new SaveLevelStateJob(levelEditorScreen.level.state);
      job.setCallback(this, LEVEL_STATE_SAVE_CALLBACK);
      jobs.enqueue(job);
      jobs.enqueue(new SaveLevelGeometryJob(levelEditorScreen.level));
      levelEditorScreen.changeManager.clear();
    }
  }

  public boolean closeAndSaveChangesMap() {
    if (levelEditorScreen != null && levelEditorScreen.changeManager != null && levelEditorScreen.changeManager.canUndo()) {

      int response = JOptionPane.showOptionDialog(mainWindow,
          "There are changes in map. Do you want to save them?",
          "Save map",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          null,
          null);

      if (response == 0) {
        saveMap();
        closeMap();
        return true;
      } else if (response == 1) {
        closeMap();
        return true;
      } else {
        return false;
      }
    } else {
      closeMap();
      return true;
    }
  }


  public void deleteFolder(String pathFile) {
    if (closeAndSaveChangesMap()) {
      jobs.waitForAllToComplete();
      int response = JOptionPane.showOptionDialog(mainWindow,
          "Are you sure?",
          "Delete folder",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          null,
          null);

      if (response == 0) {
        ForgE.log(TAG, "Removing dir: " + pathFile);
        try {
          FileUtils.deleteDirectory(new File(pathFile));
        } catch (IOException e) {
          e.printStackTrace();
        }

        triggerMapStructureChange();
      }
    }
  }

  public void createFolder(String pathFile) {
    if (closeAndSaveChangesMap()) {
      jobs.waitForAllToComplete();
      String folderName = JOptionPane.showInputDialog("Enter folder name:");
      if (folderName.length() >= 1) {
        File file = new File(pathFile + File.separator + folderName);
        ForgE.log(TAG, "Creating directory" + file.getAbsolutePath());
        file.mkdirs();
        triggerMapStructureChange();
      }
    }
  }

  public void moveMap(String source, String target) {
    if (closeAndSaveChangesMap()) {
      jobs.waitForAllToComplete();

      try {
        Path sourcePath = FileSystems.getDefault().getPath(source);
        Path targetPath = FileSystems.getDefault().getPath(target, sourcePath.getFileName().toString());

        ForgE.log(TAG, "Move " + sourcePath + " to " + targetPath);
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        triggerMapStructureChange();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void deleteMap(int levelStateId) {
    if (closeAndSaveChangesMap()) {
      jobs.waitForAllToComplete();
      int response = JOptionPane.showOptionDialog(mainWindow,
          "Are you sure?",
          "Delete map",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          null,
          null);

      if (response == 0) {
        FileHandle levelHandle = ForgE.levels.getFileHandle(levelStateId);
        File  geoHandle        = ForgE.levels.getGeoFile(levelStateId);
        ForgE.log(TAG, "Removing map: " + levelHandle.file().getAbsolutePath());

        levelHandle.file().delete();
        if (geoHandle.exists())
          geoHandle.delete();
        triggerMapStructureChange();
      }
    }
  }

  private void triggerMapStructureChange() {
    for (OnMapChangeListener listener : onMapChangeListenerArray) {
      listener.onProjectStructureChange(ProjectController.this);
    }
  }

  public void closeMap() {
    mainWindow.setTitle("");
    if (levelEditorScreen != null) {
      for (OnMapChangeListener listener : onMapChangeListenerArray) {
        listener.onCloseMap(ProjectController.this, ProjectController.this.levelEditorScreen);
      }

      Gdx.app.postRunnable(new Runnable() {
        @Override
        public void run() {
          ForgE.screens.disposeCurrentScreen();
          System.gc();
        }
      });
    }
    currentLevelState = null;
    levelEditorScreen = null;
    updateUI();
  }

  public void onLevelStateSaved(LevelState state, SaveLevelStateJob job) {
    mainWindow.setTitle(state.name);
    Gdx.app.postRunnable(new Runnable() {
      @Override
      public void run() {
        updateUI();

        for (OnMapChangeListener listener : onMapChangeListenerArray) {
          listener.onMapSaved(ProjectController.this, ProjectController.this.levelEditorScreen);
        }
      }
    });
  }

  public void onLevelStateLoaded(LevelState state, NewLevelJob job) {
    setState(state);
  }

  public void onLevelStateLoaded(LevelState state, LoadLevelJob job) {
    setState(state);
  }

  private void setState(LevelState state) {
    currentLevelState = state;
    ForgE.time.setDuration(TimeManager.DEFAULT_TIME);
    ForgE.db.lastOpenedMapId = state.id;
    mainWindow.setTitle(state.name);
    ForgE.db.save();

    if (levelEditorScreen != null) {
      levelEditorScreen.removeAfterRenderListener(this);
    }

    this.levelEditorScreen = new LevelEditorScreen(state, jobs);
    levelEditorScreen.addAfterRenderListener(this);
    Gdx.app.postRunnable(new Runnable() {
      @Override
      public void run() {
        ForgE.screens.set(levelEditorScreen);

        updateUI();

        for (OnMapChangeListener listener : onMapChangeListenerArray) {
          listener.onNewMap(ProjectController.this, ProjectController.this.levelEditorScreen);
        }
      }
    });
  }

  @Override
  public void forgeAfterRenderCallback(LevelEditorScreen screen) {

    if (updateStatusBar != null) {
      updateStatusBar.update();
    }
  }

  public void setStatusLabel(JLabel statusLabel, JLabel statusMemoryLabel, JLabel statusRenderablesLabel, JLabel mapCursorPositionLabel, JLabel statusTriangleCountLabel) {
    this.updateStatusBar = new UpdateStatusBar(this, statusLabel, statusMemoryLabel, statusRenderablesLabel, mapCursorPositionLabel, statusTriangleCountLabel);
  }

  public void addOnMapChangeListener(OnMapChangeListener listener) {
    if (!onMapChangeListenerArray.contains(listener, true)) {
      onMapChangeListenerArray.add(listener);
    }
  }

  @Override
  public void onJobStart(final Job job) {
    if (levelEditorScreen != null)
      levelEditorScreen.pause();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (job.isBlockingUI()) {
          progressTaskDialog.setLocationRelativeTo(mainWindow);
          progressTaskDialog.setVisible(true);
          mainWindow.setEnabled(false);
        }

        jobProgressBar.setVisible(true);
      }
    });
  }

  @Override
  public void onJobError(Job job, Exception e) {
    JOptionPane.showMessageDialog(mainWindow,
        e.toString(),
        "Job error",
        JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void onJobFinish(Job job) {
    if (levelEditorScreen != null)
      levelEditorScreen.resume();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jobProgressBar.setVisible(false);
        progressTaskDialog.setVisible(false);
        mainWindow.setEnabled(true);
      }
    });
  }

  @Override
  public void onShadersReload(ShadersManager shaderManager) {

  }

  @Override
  public void onShaderError(ShadersManager shaderManager, BaseShader program) {
    /*final BaseShader.Error err = error;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        TaskDialogs.showException(err);
      }
    });*/

  }

  public void rebuildChunks() {
    if (levelEditorScreen != null) {
      levelEditorScreen.level.terrainMap.rebuildAll();
    }

  }

  public void clearUndoRedo() {
    if (levelEditorScreen != null) {
      levelEditorScreen.changeManager.clear();
    }
  }

  public LevelState getCurrentLevelState() {
    return currentLevelState;
  }

  public void tryOpenLastMap() {
    if (ForgE.levels.exists(ForgE.db.lastOpenedMapId)) {
      openMap(ForgE.levels.getFileHandle(ForgE.db.lastOpenedMapId));
    }
  }

  public boolean haveOpenedMap() {
    return levelEditorScreen != null;
  }

  public void resume() {
    if (levelEditorScreen != null) {
      levelEditorScreen.resume();
    }
  }

  public void pause() {
    if (levelEditorScreen != null) {
      levelEditorScreen.pause();
    }
  }
}

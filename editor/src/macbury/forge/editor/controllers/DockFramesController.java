package macbury.forge.editor.controllers;

import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.menu.SingleCDockableListMenuPiece;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;
import com.badlogic.gdx.utils.GdxRuntimeException;
import macbury.forge.editor.windows.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Created by macbury on 07.05.15.
 */
public class DockFramesController implements MainToolbarController.EditorModeListener {
  private final CControl control;
  public final RootMenuPiece menu;
  private final DefaultCDockable mapEditorDockable;
  private final DefaultSingleCDockable terrainToolsDockable;
  private final DefaultSingleCDockable objectInspectorDockable;
  private final DefaultSingleCDockable resourcesDockable;
  private final DefaultSingleCDockable objectsDockable;
  private final DefaultSingleCDockable mapTreeDockable;
  private final CBaseLocation base;
  private final DefaultSingleCDockable terrainInspectorDockable;
  public final DefaultSingleCDockable shaderErrorDockable;

  public DockFramesController(MainWindow mainWindow) {
    control = new CControl( mainWindow );
    base    = CLocation.base();
    control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
    mainWindow.mainContentPane.add(control.getContentArea(), BorderLayout.CENTER);

    this.mapEditorDockable = new DefaultSingleCDockable( "Map", "Map", mainWindow.openGlContainer );
    //mapEditorDockable = new DefaultMultipleCDockable("Map", "Map", mainWindow.openGlContainer);
    //mapEditorDockable.setExtendedMode(ExtendedMode.MAXIMIZED);
    mapEditorDockable.setLocation(base.normal());
    mapEditorDockable.setSticky(false);
    mapEditorDockable.setCloseable(false);
    mapEditorDockable.setMaximizable(false);
    mapEditorDockable.setStackable(false);
    mapEditorDockable.setSingleTabShown(true);
    mapEditorDockable.setMinimizable(false);
    mapEditorDockable.setExternalizable(false);

    this.terrainToolsDockable     = createDockablePanel("Terrain", mainWindow.terrainPanel, true);
    this.resourcesDockable        = createDockablePanel("Resources", mainWindow.resourcesController.buildTree(), true);
    this.objectsDockable          = createDockablePanel("Objects", new JScrollPane(new JTree()), true);
    this.objectInspectorDockable  = createDockablePanel("Object Properties", mainWindow.objectInspectorContainerPanel, true);
    this.terrainInspectorDockable = createDockablePanel("Terrain Properties", mainWindow.terrainInspectorPanel, true);
    this.mapTreeDockable          = createDockablePanel("Maps", mainWindow.mapTreeScroll, true);
    this.shaderErrorDockable      = createDockablePanel("Shader Error", mainWindow.shadersController.buildLogs(), true);
    mainWindow.shadersController.setDockable(shaderErrorDockable);
    CGrid grid = new CGrid( control );

    grid.add( 2, 1, 1, 2, mapTreeDockable);
    grid.add( 2, 1, 1, 2, shaderErrorDockable);
    grid.add( 0, 0, 1, 2, terrainToolsDockable );

    grid.add( 0, 0, 2, 1, resourcesDockable );
    grid.add( 0, 1, 2, 1, objectsDockable );
    grid.add( 0, 2, 2, 1, objectInspectorDockable);
    grid.add( 0, 2, 2, 1, terrainInspectorDockable);


/*

    grid.add( 2, 0, 9, 3, createDockablePanel("Code", sp, true));
*/
    grid.add(2, 0, 9, 3, mapEditorDockable);

    control.getContentArea().deploy(grid);

    mapTreeDockable.setLocation(base.minimalWest());
    shaderErrorDockable.setLocation(base.minimalWest());

    this.menu = new RootMenuPiece( "Panels", false );
    menu.add( new SingleCDockableListMenuPiece( control ));
  }

  public DefaultSingleCDockable createDockablePanel( String title, Component panel, boolean closeable) {
    DefaultSingleCDockable dockable = new DefaultSingleCDockable( title, title, panel);
    dockable.setCloseable( closeable );
    dockable.setExternalizable(false);
    return dockable;
  }

  public DefaultSingleCDockable createDockable( String title, Color color ) {
    JPanel panel = new JPanel();
    panel.setBackground( color );
    DefaultSingleCDockable dockable = new DefaultSingleCDockable( title, title, panel);
    dockable.setCloseable( true );
    dockable.setExternalizable(false);
    return dockable;
  }

  @Override
  public void onEditorModeChange(MainToolbarController.EditorMode editorMode) {
    boolean objectsDock = false;
    boolean terrainDock = false;

    switch (editorMode) {
      case Terrain:
        objectsDock = false;
        terrainDock = true;
        break;
      case Objects:
        objectsDock = true;
        terrainDock = false;
        break;
      case None:
        objectsDock = false;
        terrainDock = false;
        break;
      default: throw new GdxRuntimeException("No support for: " + editorMode);
    }

    terrainInspectorDockable.setVisible(terrainDock);
    terrainToolsDockable.setVisible(terrainDock);
    objectInspectorDockable.setVisible(objectsDock);
    objectsDockable.setVisible(objectsDock);
    resourcesDockable.setVisible(objectsDock);
  }
}

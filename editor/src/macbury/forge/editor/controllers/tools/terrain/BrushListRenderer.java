package macbury.forge.editor.controllers.tools.terrain;

import macbury.forge.editor.controllers.tools.terrain.properties.CustomBrushChangeableProvider;

import javax.swing.*;
import java.awt.*;

/**
 * Created by macbury on 18.03.15.
 */
public class BrushListRenderer extends DefaultListCellRenderer {

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    CustomBrushChangeableProvider.BrushType brushType = (CustomBrushChangeableProvider.BrushType)value;
    if (brushType != null) {
      label.setText("");
      label.setIcon(brushType.getImageIcon());
    }

    return label;
  }
}

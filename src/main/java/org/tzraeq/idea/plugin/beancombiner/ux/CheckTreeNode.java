package org.tzraeq.idea.plugin.beancombiner.ux;

import javax.swing.*;

public interface CheckTreeNode extends TristateCheckBox.CheckListener {
    default void setChecked(boolean checked){}
    default boolean getChecked(){return true;}
    default String getLabel(){return toString();}
    default Icon getIcon(Icon icon) {
        return icon;
    }
}

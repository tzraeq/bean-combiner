package org.tzraeq.idea.plugin.beancombiner.ui;

import com.intellij.icons.AllIcons;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import javax.swing.*;

public class FieldNode extends ConfigTreeTableNode {

    private Config.Mapping.Combine.Field field;

    public FieldNode(Config.Mapping.Combine.Field field) {
        super(field);
        this.field = field;
    }

    @Override
    public Object getValueAt(int column) {
        switch (column) {
            case COLUMN_CLASS:
                return field.getSource();
            case COLUMN_FIELD:
                return field.getTarget();
            default:
                return null;
        }
    }

    @Override
    public boolean isEditable(int column) {
        return column == COLUMN_FIELD;
    }

    @Override
    public void setValueAt(Object value, int column) {
        switch (column) {
            case COLUMN_CLASS:
                break;
            case COLUMN_FIELD:
                field.setTarget((String) value);
            default:
                break;
        }
    }

    @Override
    public String getLabel() {
        return field.getSource();
    }

    @Override
    public Icon getIcon(Icon icon) {
        return AllIcons.Nodes.Field;
    }

    @Override
    public void setChecked(boolean checked) {
        field.setEnabled(checked);
    }

    public boolean getChecked() {
        return field.getEnabled();
    }
}

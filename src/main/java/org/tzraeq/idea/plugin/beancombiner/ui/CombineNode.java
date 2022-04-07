package org.tzraeq.idea.plugin.beancombiner.ui;

import com.intellij.icons.AllIcons;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import javax.swing.*;

public class CombineNode extends ConfigTreeTableNode {

    private Config.Mapping.Combine combine;

    public CombineNode(Config.Mapping.Combine combine) {
        super(combine);
        this.combine = combine;
        for (Config.Mapping.Combine.Field field:
                combine.getFields()) {
            add(new FieldNode(field));
        }
    }

    @Override
    public Object getValueAt(int column) {
        switch (column) {
            case COLUMN_CLASS:
                return combine.getFrom();
            default:
                return null;
        }
    }

    @Override
    public String getLabel() {
        return combine.getFrom();
    }

    @Override
    public Icon getIcon(Icon icon) {
        return AllIcons.Nodes.Class;
    }
}

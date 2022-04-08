package org.tzraeq.idea.plugin.beancombiner.ui;

import com.intellij.icons.AllIcons;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import javax.swing.*;

public class MappingNode extends ConfigTreeTableNode {

    private Config.Mapping mapping;

    public MappingNode(Config.Mapping mapping) {
        super(mapping);
        this.mapping = mapping;
        for (Config.Mapping.Combine combine :
                mapping.getCombine()) {
            add(new CombineNode(combine));
        }
    }

    @Override
    public Object getValueAt(int column) {
        switch (column) {
            case COLUMN_CLASS:
                return mapping.getTarget();
            default:
                return null;
        }
    }

    @Override
    public String getLabel() {
        return mapping.getTarget();
    }

    @Override
    public Icon getIcon(Icon icon) {
        return AllIcons.Nodes.Class;
    }

    public CombineNode add(Config.Mapping.Combine combine) {
        mapping.getCombine().add(combine);
        CombineNode node = new CombineNode(combine);
        add(node);
        return node;
    }

    public void remove(CombineNode node) {
        super.remove(node);
        mapping.getCombine().remove(node.getUserObject());
    }
}

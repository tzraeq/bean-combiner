package org.tzraeq.idea.plugin.beancombiner.ui;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.tzraeq.idea.plugin.beancombiner.ux.CheckTreeNode;

public abstract class ConfigTreeTableNode extends AbstractMutableTreeTableNode implements CheckTreeNode {

    public final static int COLUMN_CLASS = 0;
    public final static int COLUMN_FIELD = 1;
    public final static int COLUMN_ENABLED = 2;

    public ConfigTreeTableNode(Object object) {
        super(object);
    }

    @Override
    public Object getValueAt(int column) {
        return null;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }
}

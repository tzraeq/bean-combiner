package org.tzraeq.idea.plugin.beancombiner.ui;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class ConfigTreeTableModel extends AbstractTreeTableModel {

    private String[] columnNames = {"", "Target"/*, "Enabled"*/};
    private Class[] columnTypes = {String.class, String.class/*, Boolean.class*/};

    public ConfigTreeTableModel() {
        super();
    }

    public ConfigTreeTableModel(Config.Mapping mapping) {
        super(new MappingNode(mapping));
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnTypes[column];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        ConfigTreeTableNode ttn = (ConfigTreeTableNode) node;
        return ttn.getValueAt(column);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((TreeTableNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TreeTableNode) parent).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((TreeTableNode) parent).getIndex((TreeTableNode) child);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return ((TreeTableNode) node).isEditable(column);
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        ((TreeTableNode) node).setValueAt(value, column);
    }

    public List getCheckedNodes() {
        return getCheckedNodes((ConfigTreeTableNode)getRoot());
    }

    private List getCheckedNodes(ConfigTreeTableNode treeTableNode) {
        List<ConfigTreeTableNode> list = new ArrayList<>();
        if(null != treeTableNode) {
            int childCount = treeTableNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ConfigTreeTableNode child = (ConfigTreeTableNode)treeTableNode.getChildAt(i);
                if(child.isLeaf()) {
                    if(child.getChecked()) {
                        list.add(child);
                    }
                }else{
                    list.addAll(getCheckedNodes(child));
                }
            }
        }
        return list;
    }

    public TreeTableNode addCombine(Config.Mapping.Combine combine) {
        MappingNode mappingNode = (MappingNode) getRoot();
        CombineNode node = mappingNode.add(combine);
        modelSupport.fireChildAdded(new TreePath(getPathToRoot(node.getParent())), node.getParent().getIndex(node), node);
        return node;
    }

    public TreeTableNode[] getPathToRoot(TreeTableNode aNode) {
        List<TreeTableNode> path = new ArrayList<TreeTableNode>();
        TreeTableNode node = aNode;

        while (node != root) {
            path.add(0, node);

            node = node.getParent();
        }

        if (node == root) {
            path.add(0, node);
        }

        return path.toArray(new TreeTableNode[0]);
    }

    public void setRoot(Config.Mapping mapping) {
        this.root = new MappingNode(mapping);
        this.modelSupport.fireNewRoot();
    }
}

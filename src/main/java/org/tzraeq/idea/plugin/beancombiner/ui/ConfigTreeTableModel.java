package org.tzraeq.idea.plugin.beancombiner.ui;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import javax.swing.tree.TreeNode;
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
        return getCheckedNodes((TreeTableNode)getRoot());
    }

    public List getCheckedNodes(TreeTableNode treeTableNode) {
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

    /**
     * 添加后选中状态不能正确显示
     * @param combine
     * @return
     */
    public TreeTableNode addCombine(Config.Mapping.Combine combine) {
        MappingNode mappingNode = (MappingNode) getRoot();
        CombineNode node = mappingNode.add(combine);
        modelSupport.fireChildAdded(new TreePath(getPathToRoot(node.getParent())), node.getParent().getIndex(node), node);
        return node;
    }

    public void removeCombineNode(CombineNode node) {
        MappingNode mappingNode = (MappingNode)node.getParent();
        int index = mappingNode.getIndex(node);
        mappingNode.remove(node);
        modelSupport.fireChildRemoved(new TreePath(getPathToRoot(mappingNode)), index, node);
    }

    public TreeNode[] getPathToRoot(TreeNode aNode) {
        List<TreeNode> path = new ArrayList<TreeNode>();
        TreeNode node = aNode;

        while (node != root) {
            path.add(0, node);

            node = node.getParent();
        }

        if (node == root) {
            path.add(0, node);
        }

        return path.toArray(new TreeNode[0]);
    }

    public void setRoot(Config.Mapping mapping) {
        if(null != mapping) {
            this.root = new MappingNode(mapping);
        } else {
            this.root = null;
        }
        this.modelSupport.fireNewRoot();
    }
}

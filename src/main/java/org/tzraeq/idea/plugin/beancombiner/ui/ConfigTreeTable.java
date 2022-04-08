package org.tzraeq.idea.plugin.beancombiner.ui;

import org.jdesktop.swingx.JXTreeTable;
import org.tzraeq.idea.plugin.beancombiner.ux.CheckTreeTableManager;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

public class ConfigTreeTable extends JXTreeTable {

    private ConfigTreeTableModel treeModel;
    private CheckTreeTableManager treeTableManager;

    public ConfigTreeTable(ConfigTreeTableModel treeModel) {
        super(treeModel);
        this.treeModel = treeModel;
        treeTableManager = new CheckTreeTableManager(this);
        List checkedNodes = treeModel.getCheckedNodes();
        treeTableManager.getSelectionModel().addPathsByNodes(checkedNodes);
    }

    public void refreshSelection() {
        List checkedNodes = ((ConfigTreeTableModel)getTreeTableModel()).getCheckedNodes();
        treeTableManager.getSelectionModel().addPathsByNodes(checkedNodes);
    }

    public void expand(TreeNode node) {
        TreeNode[] path = treeModel.getPathToRoot(node);
        expandPath(new TreePath(path));
    }

    public void addPathsByNodes(List<TreeNode> selectedNodes) {
        treeTableManager.getSelectionModel().addPathsByNodes(selectedNodes);
    }

    public boolean isSelected(TreeNode node, boolean dig) {
        return treeTableManager.getSelectionModel().isPathSelected(new TreePath(treeModel.getPathToRoot(node)), dig);
    }

    public boolean isSelected(TreeNode node) {
        return isSelected(node, false);
    }

    /**
     * 这个方法验证失败
     * @param selectedNodes
     * @param expand
     */
    /*public void addPathsByNodes(List<TreeNode> selectedNodes, boolean expand) {
        if(expand){
            TreePath[] paths = new TreePath[selectedNodes.size()];
            for (int i = 0; i < selectedNodes.size(); i++) {
                TreeNode node = selectedNodes.get(i);
                TreeNode[] path = treeModel.getPathToRoot(node);
                paths[i] = new TreePath(path);
                treeTableManager.getSelectionModel().addSelectionPath(paths[i]);
                expandPath(paths[i]);
            }
        }else{
            treeTableManager.getSelectionModel().addPathsByNodes(selectedNodes);
        }
    }*/
}

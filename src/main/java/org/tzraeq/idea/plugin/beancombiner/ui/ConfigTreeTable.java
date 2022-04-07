package org.tzraeq.idea.plugin.beancombiner.ui;

import org.jdesktop.swingx.JXTreeTable;
import org.tzraeq.idea.plugin.beancombiner.ux.CheckTreeTableManager;

import java.util.List;

public class ConfigTreeTable extends JXTreeTable {

    private CheckTreeTableManager treeTableManager;

    public ConfigTreeTable(ConfigTreeTableModel treeModel) {
        super(treeModel);
        treeTableManager = new CheckTreeTableManager(this);
        List checkedNodes = treeModel.getCheckedNodes();
        treeTableManager.getSelectionModel().addPathsByNodes(checkedNodes);
        /*treeTableManager.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            // NOTE 状态发生变化的节点都会触发一次
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                boolean checked = true;
                if(null == e.getNewLeadSelectionPath()) {
                    checked = true;
                } else {

                }
            }
        });*/
    }

    public void refreshSelection() {
        List checkedNodes = ((ConfigTreeTableModel)getTreeTableModel()).getCheckedNodes();
        treeTableManager.getSelectionModel().addPathsByNodes(checkedNodes);
    }
}

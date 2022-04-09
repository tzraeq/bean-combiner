package org.tzraeq.idea.plugin.beancombiner.window;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.tzraeq.idea.plugin.beancombiner.config.Config;
import org.tzraeq.idea.plugin.beancombiner.ui.CombineNode;
import org.tzraeq.idea.plugin.beancombiner.ui.ConfigTreeTable;
import org.tzraeq.idea.plugin.beancombiner.ui.ConfigTreeTableModel;
import org.tzraeq.idea.plugin.beancombiner.ui.ConfigTreeTableNode;
import org.tzraeq.idea.plugin.beancombiner.util.CombinerUtil;
import org.tzraeq.idea.plugin.beancombiner.util.ConfigUtil;
import org.tzraeq.idea.plugin.beancombiner.util.NotificationUtil;
import org.tzraeq.idea.plugin.beancombiner.util.PsiClassUtil;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombineWindow {
    private JBPanel content;

    private ConfigTreeTable configTreeTable;
    private ConfigTreeTableModel treeTableModel;
    private ConfigTreeTableNode node;

    public CombineWindow(Project project, ToolWindow toolWindow) {

        init();
        EditorListener editorListener = new EditorListener();
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,editorListener);

        EditorFactory.getInstance().addEditorFactoryListener(editorListener, project);
        // 当前已经打开的Editor是没有监听的
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            addCaretListener(editor, editorListener);
        }

        // 分析当前打开的Editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(null != editor) {
            // NOTE 如果IDEA刚启动，这个时候有很大几率正在刷新索引，所以必须用下面的方式
            DumbService.getInstance(editor.getProject()).runWhenSmart(() -> {
                loadConfig(editor, false);
            });
        }
    }

    private void init() {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new RefreshAction());
        actions.add(new AddAction());
        actions.add(new RemoveAction());
        actions.addSeparator();
        actions.add(new ApplyAction());
        actions.add(new ExpandAllAction());
        actions.add(new CollapseAllAction());

        ActionToolbar toolbar = actionManager.createActionToolbar("BeanCombinerToolBar", actions, true);
        toolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);

        content = new JBPanel();
        /*content.setLayout(new VerticalLayout(0));
        content.add(VerticalLayout.TOP, toolbar.getComponent());
        JBScrollPane body = new JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(VerticalLayout.CENTER, body);*/
        content.setLayout(new BorderLayout());
        content.add(BorderLayout.NORTH, toolbar.getComponent());

        treeTableModel = new ConfigTreeTableModel();
        configTreeTable = new ConfigTreeTable(treeTableModel);
        configTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configTreeTable.setRootVisible(true); //  显示根结点

        configTreeTable.addTreeSelectionListener(e -> {
            TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
            if(null != newLeadSelectionPath) {// NOTE 移除节点时也会触发
                node = (ConfigTreeTableNode) newLeadSelectionPath.getLastPathComponent();
            }
        });

        JBScrollPane scroller = new JBScrollPane(configTreeTable, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        content.add(BorderLayout.CENTER, scroller);
    }

    public JPanel getContent() {
        return content;
    }

    private Config.Mapping mapping = null;
    private PsiClass psiClass = null;

    private void loadConfig(Editor editor, boolean force) {
        PsiClass psiClass = PsiClassUtil.getPsiClassByEditor(editor);
        if(null != psiClass) {
            if(this.psiClass != psiClass || force) {// NOTE 有变更才进行刷新
                this.psiClass = psiClass;
                mapping = null;
                try {
                    Config config = ConfigUtil.load(ModuleUtilCore.findModuleForPsiElement(psiClass));

                    if(null != config) {
                        List<Config.Mapping> mappingList = config.getMapping();
                        for (Config.Mapping m : mappingList) {
                            if(m.getTarget().equals(psiClass.getQualifiedName())) {
                                mapping = m;
                                break;
                            }
                        }
                    }
                    if(null == mapping) {
                        mapping = new Config.Mapping();
                        mapping.setTarget(psiClass.getQualifiedName());
                        mapping.setCombine(new ArrayList<>());
                    } else {
                        for (Config.Mapping.Combine combine : mapping.getCombine()) {
                            combine.merge(CombinerUtil.getFields(PsiClassUtil.getPsiClassByQualifiedName(editor.getProject(), combine.getFrom())));
                        }
                    }
                    // TODO 合并到具体的类
                    // TODO 渲染到TreeTable
                    treeTableModel.setRoot(mapping);
                    configTreeTable.refreshSelection();
                    configTreeTable.expandAll();  // NOTE 展开全部节点，一定要最后做，否则会影响model中的选中值，下一版再修正吧
                } catch (IOException e) {
                    NotificationUtil.notifyError(editor.getProject(), "读取配置时发生IO异常：" + e.getMessage());
                }
            }
        }else{// NOTE 如果不是类
            // TODO 清空
        }

    }

    private void addCaretListener(Editor editor, CaretListener caretListener) {
        if(PsiUtilBase.getPsiFileInEditor(editor, editor.getProject())
                instanceof PsiJavaFile) {
//                editor.getSelectionModel().addSelectionListener(this);
            editor.getCaretModel().addCaretListener(caretListener);
        }
    }

    private void removeCaretListener(Editor editor, CaretListener caretListener) {
        if(PsiUtilBase.getPsiFileInEditor(editor, editor.getProject())
                instanceof PsiJavaFile) {
//                editor.getSelectionModel().removeSelectionListener(this);
            editor.getCaretModel().removeCaretListener(caretListener);
        }
    }

    private void clearTreeTable() {
        treeTableModel.setRoot(null);
        psiClass = null;
        mapping = null;
    }

    class EditorListener implements FileEditorManagerListener, EditorFactoryListener, CaretListener{
        // FileEditorManagerListener
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            if(event.getNewFile().getFileType() instanceof JavaFileType) {
                Editor editor = event.getManager().getSelectedTextEditor();
                loadConfig(editor, false);
            } else {
                clearTreeTable();
            }
        }

        // CaretListener
        public void caretPositionChanged(@NotNull CaretEvent event) {
            Editor editor = event.getCaret().getEditor();
            loadConfig(editor, false);
        }

        // EditorFactoryListener
        @Override
        public void editorCreated(@NotNull EditorFactoryEvent event) {
            addCaretListener(event.getEditor(), this);
        }

        public void editorReleased(@NotNull EditorFactoryEvent event) {
            removeCaretListener(event.getEditor(), this);
        }
    }

    class RefreshAction extends AnAction {

        RefreshAction() {
            super("Reload mappings of this class", "Reload mappings of this class", AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
            loadConfig(editor, true);
        }
    }

    class AddAction extends AnAction {

        AddAction() {
            super("Add a source class to the mapping", "", AllIcons.ToolbarDecorator.AddClass);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(e.getProject())
                    .createAllProjectScopeChooser("Choose a source class");
            chooser.showDialog();
            PsiClass from = chooser.getSelected();
            // TODO 解析并新增一个 Source Class，需要判断当前 Mapping 中是否已经有了
            if (null != psiClass) {
                for (Config.Mapping.Combine combine : mapping.getCombine()) {
                    if(combine.getFrom().equals(from.getQualifiedName())) {
                        return;
                    }
                }
                Config.Mapping.Combine combine = new Config.Mapping.Combine();
                combine.setFrom(from.getQualifiedName());
                combine.setFields(CombinerUtil.getFields(from, true));


                TreeTableNode treeTableNode = treeTableModel.addCombine(combine);
                if(!configTreeTable.isSelected((TreeNode)treeTableModel.getRoot())){
                    // NOTE 如果跟没有被完全选中，则需要调用下面的函数
                    List checkedNodes = treeTableModel.getCheckedNodes(treeTableNode);
                    configTreeTable.addPathsByNodes(checkedNodes);
                }
                configTreeTable.expand(treeTableNode);

                /*mapping.getCombine().add(combine);
                treeTableModel.setRoot(mapping);
                configTreeTable.refreshSelection();
                configTreeTable.expandAll();*/
            }
        }
    }

    class RemoveAction extends AnAction {

        RemoveAction() {
            super("Remove a source class from the mapping", "", AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(null != node
                    && node instanceof CombineNode) {
                treeTableModel.removeCombineNode((CombineNode) node);
            }
        }
    }

    class ApplyAction extends AnAction {

        ApplyAction() {
            super("Commit changes to this class", "", AllIcons.Actions.Commit);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
            // TODO 应用到当前类并保存到配置
            System.out.println(e);
        }
    }

    class ExpandAllAction extends AnAction {

        ExpandAllAction() {
            super("Commit changes to this class", "", AllIcons.Actions.Expandall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            configTreeTable.expandAll();
        }
    }

    class CollapseAllAction extends AnAction {

        CollapseAllAction() {
            super("Commit changes to this class", "", AllIcons.Actions.Collapseall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            configTreeTable.collapseAll();
            configTreeTable.expand(((TreeNode) treeTableModel.getRoot()));
        }
    }
}

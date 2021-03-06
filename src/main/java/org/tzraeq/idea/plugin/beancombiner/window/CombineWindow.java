package org.tzraeq.idea.plugin.beancombiner.window;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.*;
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
        // ?????????????????????Editor??????????????????
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            addCaretListener(editor, editorListener);
        }

        // ?????????????????????Editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(null != editor) {
            // NOTE ??????IDEA??????????????????????????????????????????????????????????????????????????????????????????
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
        configTreeTable.setRootVisible(true); //  ???????????????

        configTreeTable.addTreeSelectionListener(e -> {
            TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
            if(null != newLeadSelectionPath) {// NOTE ???????????????????????????
                node = (ConfigTreeTableNode) newLeadSelectionPath.getLastPathComponent();
            } else {
                node = null;
            }
        });

        JBScrollPane scroller = new JBScrollPane(configTreeTable, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        content.add(BorderLayout.CENTER, scroller);
    }

    public JPanel getContent() {
        return content;
    }

    private Config config = null;
    private Config.Mapping mapping = null;
    private PsiClass psiClass = null;

    private void loadConfig(Editor editor, boolean force) {
        PsiClass psiClass = PsiClassUtil.getPsiClassByEditor(editor);
        if(null != psiClass) {
            if(this.psiClass != psiClass || force) {// NOTE ????????????????????????
                this.psiClass = psiClass;
                mapping = null;
                try {
                    Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
                    if(null == module) {
                        NotificationUtil.notifyError(editor.getProject(), "Cannot find the module of " + psiClass.getQualifiedName());
                        return;
                    }
                    config = ConfigUtil.load(module);

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
                        config = new Config();
                        config.setMapping(new ArrayList<>(){{
                            add(mapping);
                        }});
                    } else {
                        List combineList = new ArrayList();
                        for (Config.Mapping.Combine combine : mapping.getCombine()) {
                            PsiClass fromClass = PsiClassUtil.getPsiClassByQualifiedName(editor.getProject(), combine.getFrom());
                            if (null != fromClass) {
                                combine.merge(CombinerUtil.getFields(fromClass));
                                combineList.add(combine);
                            }
                        }
                        mapping.setCombine(combineList);
                    }
                    // TODO ?????????????????????
                    // TODO ?????????TreeTable
                    treeTableModel.setRoot(mapping);
                    configTreeTable.refreshSelection();
                    configTreeTable.expandAll();  // NOTE ?????????????????????????????????????????????????????????model???????????????????????????????????????
                } catch (IOException e) {
                    NotificationUtil.notifyError(editor.getProject(), "Cannot read .beancombiner file???" + e.getMessage());
                }
            }
        }
    }

    /*private void loadConfig(PsiClass psiClass) {
        try {
            config = ConfigUtil.load(ModuleUtilCore.findModuleForPsiElement(psiClass));

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
                    combine.merge(CombinerUtil.getFields(PsiClassUtil.getPsiClassByQualifiedName(psiClass.getProject(), combine.getFrom())));
                }
            }
            // TODO ?????????????????????
            // TODO ?????????TreeTable
            treeTableModel.setRoot(mapping);
            configTreeTable.refreshSelection();
            configTreeTable.expandAll();  // NOTE ?????????????????????????????????????????????????????????model???????????????????????????????????????
        } catch (IOException e) {
            NotificationUtil.notifyError(psiClass.getProject(), "?????????????????????IO?????????" + e.getMessage());
        }
    }*/

    private void addCaretListener(Editor editor, CaretListener caretListener) {
        Project project = editor.getProject();
        if(null != project) {
            if(PsiUtilBase.getPsiFileInEditor(editor, project)
                    instanceof PsiJavaFile) {
//                editor.getSelectionModel().addSelectionListener(this);
                editor.getCaretModel().addCaretListener(caretListener);
            }
        }
    }

    private void removeCaretListener(Editor editor, CaretListener caretListener) {
        Project project = editor.getProject();
        if(null != project) {
            if (PsiUtilBase.getPsiFileInEditor(editor, editor.getProject())
                    instanceof PsiJavaFile) {
//                editor.getSelectionModel().removeSelectionListener(this);
                editor.getCaretModel().removeCaretListener(caretListener);
            }
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
            if(null != event.getNewFile()){
                if(event.getNewFile().getFileType() instanceof JavaFileType) {
                    Editor editor = event.getManager().getSelectedTextEditor();
                    DumbService.getInstance(editor.getProject()).runWhenSmart(() -> {
                        loadConfig(editor, false);
                    });
                } else {
                    clearTreeTable();
                }
            }
        }

        // CaretListener
        public void caretPositionChanged(@NotNull CaretEvent event) {
            Editor editor = event.getCaret().getEditor();
            DumbService.getInstance(editor.getProject()).runWhenSmart(() -> {
                loadConfig(editor, false);
            });
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
            if(null != psiClass) {
                Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
                loadConfig(editor, true);
            }
        }
    }

    class AddAction extends AnAction {

        AddAction() {
            super("Add a source class to the mapping", "", AllIcons.ToolbarDecorator.AddClass);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (null != psiClass) {
                TreeClassChooser chooser = TreeClassChooserFactory.getInstance(e.getProject())
                        .createAllProjectScopeChooser("Choose a source class");
                chooser.showDialog();
                PsiClass from = chooser.getSelected();
                // TODO ????????????????????? Source Class????????????????????? Mapping ?????????????????????

                if(null != from) {// NOTE bugfix 1.0.1??????????????????
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
                        // NOTE ???????????????????????????????????????????????????????????????
                        List checkedNodes = treeTableModel.getCheckedNodes(treeTableNode);
                        configTreeTable.addPathsByNodes(checkedNodes);
                    }
                    configTreeTable.expand(treeTableNode);
                }


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
            if (null != psiClass) {
//                Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
//                PsiClass target = PsiClassUtil.getPsiClassByEditor(editor);
                PsiClass target = psiClass;
                if(null != target) {
                    try {
                        ConfigUtil.store(ModuleUtilCore.findModuleForPsiElement(psiClass), config);
                        mapping.getCombine().forEach( combine -> {
                            WriteCommandAction.writeCommandAction(target.getProject())
                                    .withName("Combine From " + combine.getFrom()).run(() -> {
                                PsiField[] fields = target.getAllFields();
                                PsiClass fromClass = PsiClassUtil.getPsiClassByQualifiedName(target.getProject(), combine.getFrom());
                                combine.getFields().forEach(field -> {
                                    if (field.getEnabled()) {
                                        PsiField targetField = null;
                                        for (PsiField psiField : fields) {
                                            if (field.getTarget().equals(psiField.getName())) {// NOTE ????????????
                                                targetField = psiField;
                                                break;
                                            }
                                        }
                                        if (null == targetField) { // NOTE ????????????????????????
                                            PsiMethod getter = CombinerUtil.getGetter(fromClass, field.getSource());
                                            target.add(PsiElementFactory.getInstance(target.getProject())
                                                    .createField(field.getTarget(), getter.getReturnType()));
                                        }
                                    }
                                });
                            });
                        });

                    } catch (IOException exception) {
                        NotificationUtil.notifyError(target.getProject(), "?????????????????????IO?????????" + exception.getMessage());
                    }
                }
            }
        }
    }

    class ExpandAllAction extends AnAction {

        ExpandAllAction() {
            super("Commit changes to this class", "", AllIcons.Actions.Expandall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(null != psiClass) {
                configTreeTable.expandAll();
            }
        }
    }

    class CollapseAllAction extends AnAction {

        CollapseAllAction() {
            super("Commit changes to this class", "", AllIcons.Actions.Collapseall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(null != psiClass) {
                configTreeTable.collapseAll();
                configTreeTable.expand(((TreeNode) treeTableModel.getRoot()));
            }
        }
    }
}

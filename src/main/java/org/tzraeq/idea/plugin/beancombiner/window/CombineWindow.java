package org.tzraeq.idea.plugin.beancombiner.window;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationsManager;
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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jdesktop.swingx.treetable.SimpleFileSystemModel;
import org.jetbrains.annotations.NotNull;
import org.tzraeq.idea.plugin.beancombiner.config.Config;
import org.tzraeq.idea.plugin.beancombiner.ui.ConfigTreeTable;
import org.tzraeq.idea.plugin.beancombiner.ui.ConfigTreeTableModel;
import org.tzraeq.idea.plugin.beancombiner.util.CombinerUtil;
import org.tzraeq.idea.plugin.beancombiner.util.ConfigUtil;
import org.tzraeq.idea.plugin.beancombiner.util.NotificationUtil;
import org.tzraeq.idea.plugin.beancombiner.util.PsiClassUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CombineWindow {
    private JBPanel content;

    private ConfigTreeTable configTreeTable;

    public CombineWindow(Project project, ToolWindow toolWindow) {

        init();
        EditorListener editorListener = new EditorListener();
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,editorListener);

        EditorFactory.getInstance().addEditorFactoryListener(editorListener, project);
        // 当前已经打开的Editor是没有监听的
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
//            editor.getSelectionModel().addSelectionListener(editorListener);
            editor.getCaretModel().addCaretListener(editorListener);
        }

        // 分析当前打开的Editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(null != editor) {
            DumbService.getInstance(editor.getProject()).runWhenSmart(() -> {
                loadConfig(editor);
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

        ActionToolbar toolbar = actionManager.createActionToolbar("BeanCombinerToolBar", actions, true);
        toolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);

        content = new JBPanel();
        /*content.setLayout(new VerticalLayout(0));
        content.add(VerticalLayout.TOP, toolbar.getComponent());
        JBScrollPane body = new JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(VerticalLayout.CENTER, body);*/
        content.setLayout(new BorderLayout());
        content.add(BorderLayout.NORTH, toolbar.getComponent());

        configTreeTable = new ConfigTreeTable(new ConfigTreeTableModel());
        configTreeTable.setRootVisible(true); //  显示根结点

        JBPanel body = new JBPanel();
//        body.setBorder(BorderFactory.createMatteBorder(1,0,0,0, Color.GRAY));
//        body.setBackground(Color.WHITE);

        JBScrollPane scroller = new JBScrollPane(configTreeTable, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        content.add(BorderLayout.CENTER, scroller);
    }

    public JPanel getContent() {
        return content;
    }

    private Config.Mapping mapping = null;
    private PsiClass psiClass = null;

    private void loadConfig(Editor editor) {
        PsiClass psiClass = PsiClassUtil.getPsiClassByEditor(editor);
        if(null != psiClass) {
            if(this.psiClass != psiClass) {// NOTE 有变更才进行刷新
                this.psiClass = psiClass;
                try {
                    Config config = ConfigUtil.load(ModuleUtilCore.findModuleForPsiElement(psiClass));
                    Config.Mapping mapping = null;
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
                    ((ConfigTreeTableModel) configTreeTable.getTreeTableModel()).setRoot(mapping);
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

    class EditorListener implements FileEditorManagerListener, EditorFactoryListener, CaretListener{
        // FileEditorManagerListener
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            Editor editor = event.getManager().getSelectedTextEditor();
            loadConfig(editor);
        }

        // CaretListener
        // NOTE 会触发两次
        public void caretPositionChanged(@NotNull CaretEvent event) {
            Editor editor = event.getCaret().getEditor();
            loadConfig(editor);
        }

        // EditorFactoryListener
        @Override
        public void editorCreated(@NotNull EditorFactoryEvent event) {
//            event.getEditor().getSelectionModel().addSelectionListener(this);
            event.getEditor().getCaretModel().addCaretListener(this);
        }

        public void editorReleased(@NotNull EditorFactoryEvent event) {
//            event.getEditor().getSelectionModel().removeSelectionListener(this);
            event.getEditor().getCaretModel().removeCaretListener(this);
        }
    }

    class RefreshAction extends AnAction {

        RefreshAction() {
            super("Reload mappings of this class", "Reload mappings of this class", AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // TODO 重新读取当前类的配置，并刷新UI
            System.out.println(e);
        }
    }

    class AddAction extends AnAction {

        AddAction() {
            super("Add a source class to the mapping", "", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(e.getProject())
                    .createAllProjectScopeChooser("Choose a source class");
            chooser.showDialog();
            PsiClass psiClass = chooser.getSelected();
            // TODO 解析并新增一个 Source Class，需要判断当前 Mapping 中是否已经有了
        }
    }

    class RemoveAction extends AnAction {

        RemoveAction() {
            super("Remove a source class from the mapping", "", AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // TODO 将选择的类增从当前类移除
            System.out.println(e);
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
}

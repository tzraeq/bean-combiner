package org.tzraeq.idea.plugin.beancombiner.window;

import com.intellij.icons.AllIcons;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiClass;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.tzraeq.idea.plugin.beancombiner.util.PsiClassUtil;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CombineWindow implements EditorFactoryListener {
    private JButton hideButton;

    private JBLabel datetimeLabel;

    private JBPanel content;

    public CombineWindow(Project project, ToolWindow toolWindow) {

        init();
        EditorListener editorListener = new EditorListener();
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,editorListener);

        EditorFactory.getInstance().addEditorFactoryListener(editorListener, project);
        // 当前已经打开的Editor是没有监听的
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            editor.getCaretModel().addCaretListener(editorListener);
        }

        // 分析当前打开的Editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(null != editor) {
            // TODO
        }

        hideButton.addActionListener(e -> toolWindow.hide(null));
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
        datetimeLabel = new JBLabel();
        datetimeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        hideButton = new JButton("取消");

        content = new JBPanel();
        /*content.setLayout(new VerticalLayout(0));
        content.add(VerticalLayout.TOP, toolbar.getComponent());
        JBScrollPane body = new JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(VerticalLayout.CENTER, body);*/
        content.setLayout(new BorderLayout());
        content.add(BorderLayout.NORTH, toolbar.getComponent());
        JBPanel body = new JBPanel();
        body.setBorder(BorderFactory.createMatteBorder(1,0,0,0, Color.GRAY));
        body.setBackground(Color.WHITE);

        JBScrollPane scroller = new JBScrollPane(body, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        content.add(BorderLayout.CENTER, scroller);

        body.add(datetimeLabel);
        body.add(hideButton);
    }

    public JPanel getContent() {
        return content;
    }

    public void editorReleased(@NotNull EditorFactoryEvent editorFactoryEvent) {

    }

    class EditorListener implements FileEditorManagerListener, EditorFactoryListener, CaretListener {
        // FileEditorManagerListener
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            Editor editor = event.getManager().getSelectedTextEditor();

            PsiClass psiClass = PsiClassUtil.getPsiClassByEditor(editor);
            if(null != psiClass) {
                datetimeLabel.setText(psiClass.getQualifiedName());
            } else {
                datetimeLabel.setText(null);
            }
        }

        // CaretListener
        public void caretPositionChanged(@NotNull CaretEvent event) {
            PsiClass psiClass = PsiClassUtil.getPsiClassByEditor(event.getCaret().getEditor());
            if(null != psiClass) {
                datetimeLabel.setText(psiClass.getQualifiedName());
            } else {
                datetimeLabel.setText(null);
            }
        }

        // EditorFactoryListener
        @Override
        public void editorCreated(@NotNull EditorFactoryEvent event) {
            event.getEditor().getCaretModel().addCaretListener(this);
        }

        public void editorReleased(@NotNull EditorFactoryEvent event) {
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
            if(null != psiClass) {
                // TODO 将选择的类增加到当前类
                datetimeLabel.setText(psiClass.getQualifiedName());
            }else{
                datetimeLabel.setText(null);
            }
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

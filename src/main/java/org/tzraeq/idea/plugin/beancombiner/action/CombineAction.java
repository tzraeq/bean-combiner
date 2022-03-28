package org.tzraeq.idea.plugin.beancombiner.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.tzraeq.idea.plugin.beancombiner.config.Config;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.File;
import java.io.FileInputStream;

public class CombineAction extends AnAction {

    private static final String from = "org.tzraeq.entity.User";
    private static final String CONFIG_FILE = ".beancombiner.yml";

    private Project project;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        project = anActionEvent.getProject();

        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);

        PsiClass target = getPsiClassByEditor(editor);
        if(null != target) {
            Config config = loadConfig(ModuleUtilCore.findModuleForPsiElement(target));
            if (null != config) {
                for (Config.Mapping mapping: config.getMapping()) {
                    if(target.getQualifiedName().equals(mapping.getTarget())) {
                        // TODO 多个类应该合并属性集
                        mapping.getCombine().forEach(from -> {
                            PsiClass fromClass = getPsiClassByQualifiedName(from.getFrom());
                            if(null != fromClass) {
                                WriteCommandAction.runWriteCommandAction(project, () -> {
                                    // TODO 应该遍历get方法，而不是field
                                    // NOTE 继承来的也会被取出来
                                    PsiField[] fields = fromClass.getAllFields();
                                    for (PsiField field:
                                            fields) {
                                        target.add(PsiElementFactory.getInstance(project).createField(field.getName(), field.getType()));
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    private PsiClass getPsiClassByQualifiedName(String name) {
        final PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(name, GlobalSearchScope.allScope(project));

        return psiClass;
    }

    private PsiClass getPsiClassByEditor(Editor editor) {
        PsiClass psiClass = null;
        PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        if(element.getParent() instanceof PsiClass) {
            psiClass = (PsiClass) element.getParent();
        }
        return psiClass;
    }

    private Config loadConfig(Module module) {
        Config config = null;
        // NOTE 多模块不知道是不是适用
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
//        File file = new File(module.getModuleFile().getParent().getPath(), CONFIG_FILE);
        File file = new File(rootManager.getContentRoots()[0].getPath(), CONFIG_FILE);
        if(file.exists()) {
            try {
                config = new Yaml().loadAs(new FileInputStream(file.getCanonicalPath()), Config.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return config;
    }
}

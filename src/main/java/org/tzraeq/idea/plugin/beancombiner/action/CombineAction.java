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
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tzraeq.idea.plugin.beancombiner.config.Config;
import org.tzraeq.idea.plugin.beancombiner.util.PsiClassUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class CombineAction extends AnAction {

    private static final String from = "org.tzraeq.entity.User";
    private static final String CONFIG_FILE = ".beancombiner";

    private Project project;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        project = anActionEvent.getProject();

        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);

        PsiClass target = PsiClassUtil.getPsiClassByEditor(editor);
        if (null != target) {
            Config config = loadConfig(ModuleUtilCore.findModuleForPsiElement(target));
            if (null != config) {
                for (Config.Mapping mapping : config.getMapping()) {
                    if (target.getQualifiedName().equals(mapping.getTarget())) {
                        // NOTE 一个类一个ACTION，所以多个类合并了属性集
                        mapping.getCombine().forEach(from -> {
                            PsiClass fromClass = PsiClassUtil.getPsiClassByQualifiedName(project, from.getFrom());
                            List<Config.Mapping.Combine.Field> fromFields = from.getFields();
                            if (null != fromClass) {
                                // NOTE 目标bean按照字段的方式取出已有的字段名，因为通常都是为了编辑字段而使用本插件，配合lombok基本没有影响
                                // TODO withName 会出现在Edit菜单中的Undo XXXX，所以下一个版本要改为一个action，而不是多个
                                WriteCommandAction.writeCommandAction(project)
                                        .withName("Combine From " + fromClass.getName()).run(() -> {
                                    PsiField[] fields = target.getAllFields();
                                    PsiMethod[] methods = fromClass.getAllMethods();
                                    for (PsiMethod method :
                                            methods) {
                                        if (!method.hasParameters()
                                                && !method.getContainingClass().getQualifiedName().equals(CommonClassNames.JAVA_LANG_OBJECT)) {
                                            String fieldName = getFieldName(method.getName());
                                            if (null != fieldName) {// NOTE 符合get或者is方法命名规则
                                                if (null != fromFields) { // NOTE 有进行筛选，有UI的情况下肯定 size > 0
                                                    Config.Mapping.Combine.Field fromField = null;
                                                    for (Config.Mapping.Combine.Field field :
                                                            fromFields) {
                                                        if (field.getSource().equals(fieldName)) {// NOTE 有匹配的配置项
                                                            fromField = field;
                                                            break;
                                                        }
                                                    }
                                                    if (null != fromField) { // NOTE 有匹配的配置项
                                                        fieldName = fromField.getTarget();
                                                    } else { // NOTE 没有匹配的配置项
                                                        continue;
                                                    }
                                                }
                                                PsiField targetField = null;
                                                for (PsiField field :
                                                        fields) {
                                                    if (field.getName().equals(fieldName)) {// NOTE 重复
                                                        targetField = field;
                                                        break;
                                                    }
                                                }
                                                if (null == targetField) {
                                                    target.add(PsiElementFactory.getInstance(project)
                                                            .createField(fieldName, method.getReturnType()));
                                                }
                                            }
                                        }
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

    /**
     * 获得字段名，如果是非 get 和 is 方法，则返回null
     *
     * @param methodName
     * @return
     */
    @Nullable
    public String getFieldName(String methodName) {

        String name = methodName;
        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        } else {
            return null;
        }

        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    @Nullable
    public String getFieldName(PsiMethod method) {
        if (!method.hasParameters()) {
            return getFieldName(method.getName());
        } else {
            return null;
        }
    }

    private Config loadConfig(Module module) {
        Config config = null;
        // NOTE 多模块不知道是不是适用
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
//        File file = new File(module.getModuleFile().getParent().getPath(), CONFIG_FILE);
        File file = new File(rootManager.getContentRoots()[0].getPath(), CONFIG_FILE);
        if (file.exists()) {
            try {
                config = new Yaml().loadAs(new FileInputStream(file.getCanonicalPath()), Config.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return config;
    }
}

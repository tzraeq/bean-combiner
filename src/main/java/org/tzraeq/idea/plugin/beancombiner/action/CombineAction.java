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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tzraeq.idea.plugin.beancombiner.config.Config;
import org.tzraeq.idea.plugin.beancombiner.util.PsiClassUtil;
import org.yaml.snakeyaml.Yaml;

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

        PsiClass target = PsiClassUtil.getPsiClassByEditor(editor);
        if(null != target) {
            Config config = loadConfig(ModuleUtilCore.findModuleForPsiElement(target));
            if (null != config) {
                for (Config.Mapping mapping: config.getMapping()) {
                    if(target.getQualifiedName().equals(mapping.getTarget())) {
                        // TODO 多个类应该合并属性集
                        mapping.getCombine().forEach(from -> {
                            PsiClass fromClass = PsiClassUtil.getPsiClassByQualifiedName(project, from.getFrom());
                            if(null != fromClass) {
                                // NOTE 目标bean按照字段的方式取出已有的字段名，因为通常都是为了编辑字段而使用本插件
                                /*List<String> targetFieldNameList = new ArrayList<>();
                                PsiMethod[] targetAllMethods = target.getAllMethods();
                                for (PsiMethod method :
                                        targetAllMethods) {
                                    if(!method.hasParameters()) {
                                        String fieldName = getFieldName(method);
                                        if(null != fieldName) {
                                            targetFieldNameList.add(fieldName);
                                        }
                                    }
                                }*/
//                                ApplicationManager.getApplication().runWriteAction(
                                // NOTE withName 会出现在Edit菜单中的Undo XXXX，所以下一个版本要改为一个action，而不是多个
                                WriteCommandAction.writeCommandAction(project).withName("Combine From " + fromClass.getName()).run(() -> {
                                    PsiField[] fields = target.getAllFields();
                                    PsiMethod[] methods = fromClass.getAllMethods();
                                    for (PsiMethod method :
                                            methods) {
                                        if(!method.getContainingClass().getName().equals("Object") && !method.hasParameters()) {
                                            String fieldName = getFieldName(method.getName());
                                            if(null != fieldName) {
                                                PsiField targetField = null;
                                                for (PsiField field:
                                                        fields) {
                                                    if(field.getName().equals(fieldName)){// NOTE 重复
                                                        targetField = field;
                                                        break;
                                                    }
                                                }
                                                if(null == targetField) {
                                                    target.add(PsiElementFactory.getInstance(project).createField(fieldName, method.getReturnType()));
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
        if(!method.hasParameters()) {
            return getFieldName(method.getName());
        }else{
            return null;
        }
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

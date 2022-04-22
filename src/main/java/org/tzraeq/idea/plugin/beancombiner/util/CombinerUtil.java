package org.tzraeq.idea.plugin.beancombiner.util;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import java.util.ArrayList;
import java.util.List;

public class CombinerUtil {
    /**
     * 获得字段名，如果是非 get 和 is 方法，则返回null
     *
     * @param methodName
     * @return
     */
    @Nullable
    public static String getFieldName(String methodName) {

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

    /**
     * 获得PsiClass所有的get和is方法的字段名
     * @param clazz
     * @return
     */
    public static List<Config.Mapping.Combine.Field> getFields(PsiClass clazz) {
        return getFields(clazz, false);
    }

    public static List<Config.Mapping.Combine.Field> getFields(PsiClass clazz, boolean defaultEnabled) {
        List<Config.Mapping.Combine.Field> fields = new ArrayList<>();
        PsiMethod[] methods = clazz.getAllMethods();
        for (PsiMethod method : methods) {
            if (!method.hasParameters()
                    && !method.getContainingClass().getQualifiedName().equals(CommonClassNames.JAVA_LANG_OBJECT)) {
                String fieldName = CombinerUtil.getFieldName(method.getName());
                if(null != fieldName) { // NOTE bugfix 1.0.1版本之前没有判断
                    Config.Mapping.Combine.Field field = new Config.Mapping.Combine.Field(fieldName, fieldName);
                    field.setEnabled(defaultEnabled);
                    fields.add(field);
                }
            }
        }

        return fields;
    }

    /**
     * 根据字段名在对象中搜索方法，按照java bean的规则，不能同时存在同名字段的get和is方法
     * @param clazz
     * @param fieldName
     * @return
     */
    @Nullable
    public static PsiMethod getGetter(PsiClass clazz, String fieldName) {
        PsiMethod[] methods = clazz.getAllMethods();
        for (PsiMethod method : methods) {
            String name = getFieldName(method.getName());
            if(fieldName.equals(name)) {
                return method;
            }
        }
        return null;
    }
}

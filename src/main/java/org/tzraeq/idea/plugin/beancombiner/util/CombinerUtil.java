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
        List<Config.Mapping.Combine.Field> fields = new ArrayList<>();
        PsiMethod[] methods = clazz.getAllMethods();
        for (PsiMethod method : methods) {
            if (!method.hasParameters()
                    && !method.getContainingClass().getQualifiedName().equals(CommonClassNames.JAVA_LANG_OBJECT)) {
                String fieldName = CombinerUtil.getFieldName(method.getName());
                fields.add(new Config.Mapping.Combine.Field(fieldName, fieldName));
            }
        }

        return fields;
    }
}

package org.tzraeq.idea.plugin.beancombiner.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 关于获取PsiClass的工具类
 */
public class PsiClassUtil {

    /**
     * 根据限定名，在最大范围内搜索
     *
     * @param project
     * @param name
     * @return
     */
    public static PsiClass getPsiClassByQualifiedName(@NotNull Project project, @NonNls @NotNull String name) {
        return getPsiClassByQualifiedName(project, name, GlobalSearchScope.allScope(project));
    }

    /**
     * 根据限定名，在指定范围内搜索
     *
     * @param project
     * @param name
     * @param scope
     * @return
     */
    @Nullable
    public static PsiClass getPsiClassByQualifiedName(@NotNull Project project, @NonNls @NotNull String name, @NotNull GlobalSearchScope scope) {
        return JavaPsiFacade.getInstance(project).findClass(name, scope);
    }

    /**
     * 通过Editor获取正在编辑的类，目前是通过光标所在位置的代码块进行上下文判断取出
     *
     * @param editor
     * @return
     */
    @Nullable
    public static PsiClass getPsiClassByEditor(@NotNull Editor editor) {
        PsiClass psiClass = null;
//        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
//        PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
//        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        PsiElement element = PsiUtilBase.getElementAtCaret(editor);// NOTE 这个方法就是封装了上面两句代码
        if(null != (psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class))){

        } else if(null != (psiClass = PsiTreeUtil.getNextSiblingOfType(element, PsiClass.class))){

        } else if(null != (psiClass = PsiTreeUtil.getPrevSiblingOfType(element, PsiClass.class))) {

        }// package和import 语句中暂时没处理，还有java文件最后一个空行没有解析出来

        return psiClass;
    }

    public static PsiClass getPsiClassByPsiElement(@NotNull PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    /**
     * 获得一个PsiIdentifier对应的PsiClass，注意，如果目标工程的build path等信息不正确，比如maven没有刷新，gradle没有同步的情况下，
     * 大部分情况都会返回null
     * @param element PsiIdentifier of PsiReference, PsiParameter, PsiField, PsiLocalVariable
     * @return
     */
    public static PsiClass getPsiClassOfPsiIdentifier(@NotNull PsiElement element) {
        element = element.getParent();
        if(element instanceof PsiReference) {
            element = ((PsiReference) element).resolve();
        }

        if(element instanceof PsiVariable) {
            return PsiTypesUtil.getPsiClass(((PsiVariable)element).getType());
        }
        return null;
    }

    /*public static PsiClass getPsiClassByCaret(Caret caret) {
        return getPsiClassByEditor(caret.getEditor());
    }*/
}

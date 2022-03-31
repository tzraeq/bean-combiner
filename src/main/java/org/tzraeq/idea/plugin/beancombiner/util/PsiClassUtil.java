package org.tzraeq.idea.plugin.beancombiner.util;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

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
    public static PsiClass getPsiClassByQualifiedName(Project project, String name) {
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
    public static PsiClass getPsiClassByQualifiedName(Project project, String name, GlobalSearchScope scope) {
        return JavaPsiFacade.getInstance(project).findClass(name, scope);
    }

    /**
     * 通过Editor获取正在编辑的类，目前是通过光标所在位置的代码块进行上下文判断取出
     *
     * @param editor
     * @return
     */
    public static PsiClass getPsiClassByEditor(Editor editor) {
        PsiClass psiClass = null;
//        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
//        PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
//        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        PsiElement element = PsiUtilBase.getElementAtCaret(editor);// NOTE 这个方法就是封装了上面两句代码
        if(null != (psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class))){

        } else if(null != (psiClass = PsiTreeUtil.getNextSiblingOfType(element, PsiClass.class))){

        } else if(null != (psiClass = PsiTreeUtil.getPrevSiblingOfType(element, PsiClass.class))) {

        }// package和import 语句中暂时没处理
        /*if (element.getParent() instanceof PsiClass) {// PsiWhiteSpace, PsiIdentifier:类名, PsiKeyword:class
            element = element.getParent();
        } else if (element.getNextSibling() instanceof PsiClass) {
            element = element.getNextSibling();
        } else if (element.getParent().getParent() instanceof PsiClass) {// PsiKeyword:public, PsiKeyword:extends
            element = element.getParent().getParent();
        } else if (element.getParent().getParent() instanceof PsiField) {
            element = element.getParent().getParent().getParent();
        } else {
            return null;
        }*/
        return psiClass;
    }

    /*public static PsiClass getPsiClassByCaret(Caret caret) {
        return getPsiClassByEditor(caret.getEditor());
    }*/
}

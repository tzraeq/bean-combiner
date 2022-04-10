package org.tzraeq.idea.plugin.beancombiner.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.BeanCombinerIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLBundle;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

public class BeanCombinerFileType extends LanguageFileType {

    public static final BeanCombinerFileType INSTANCE = new BeanCombinerFileType();
    @NonNls
    public static final String DEFAULT_EXTENSION = "beancombiner";

    private BeanCombinerFileType() {
        super(YAMLLanguage.INSTANCE);
    }

    @Override
    @NotNull
    public String getName() {
        return "beancombiner";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "BeanCombiner YAML config file";
    }

    @Override
    @NotNull
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return BeanCombinerIcons.ConfigFile;
    }
}

package org.tzraeq.idea.plugin.beancombiner.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import org.jetbrains.annotations.Nullable;
import org.tzraeq.idea.plugin.beancombiner.config.Config;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigUtil {

    private static final String CONFIG_FILE = ".beancombiner";

    /**
     * 加载对应模块下的配置
     * @param module
     * @return
     */
    public static Config load(Module module) throws IOException {
        Config config = null;
        // NOTE 多模块不知道是不是适用
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
//        File file = new File(module.getModuleFile().getParent().getPath(), CONFIG_FILE);
        File file = new File(rootManager.getContentRoots()[0].getPath(), CONFIG_FILE);
        if (file.exists()) {
            config = new Yaml().loadAs(new FileInputStream(file.getCanonicalPath()), Config.class);
        }else{
            return null;
        }

        return config;
    }

    public static void store(Module module, Config config) throws IOException {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        File file = new File(rootManager.getContentRoots()[0].getPath(), CONFIG_FILE);
        FileWriter writer = new FileWriter(file);
        new Yaml().dump(config, writer);
    }

}

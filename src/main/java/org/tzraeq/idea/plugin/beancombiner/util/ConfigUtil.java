package org.tzraeq.idea.plugin.beancombiner.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import org.tzraeq.idea.plugin.beancombiner.config.Config;

import java.io.File;
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
            ObjectMapper mapper = new ObjectMapper(YAMLFactory.builder().build());
            config = mapper.readValue(file, Config.class);
        }else{
            return null;
        }

        return config;
    }

    public static void store(Module module, Config config) throws IOException {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        File file = new File(rootManager.getContentRoots()[0].getPath(), CONFIG_FILE);
        ObjectMapper mapper = new ObjectMapper(YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) // NOTE 文档分割线
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES) // NOTE 字符串引号
                .build());
        mapper.writeValue(file, config);
    }
}

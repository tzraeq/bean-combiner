package org.tzraeq.idea.plugin.beancombiner.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class Config {
    private String version;
    private List<Mapping> mapping;

    @Getter @Setter
    public static class Mapping {
        private String target;
        private List<From> combine;

        @Getter @Setter
        public static class From {
            private String from;
            private List<Field> fields;

            @Getter @Setter
            public static class Field {
                private String source;
                private String target;

                public Field(String str) {
                    String[] tokens = str.split(",");
                    source = tokens[0].trim();
                    if(tokens.length > 1) {
                        target = tokens[1].trim();
                    }else{
                        target = source;
                    }
                }
            }
        }
    }
}

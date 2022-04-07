package org.tzraeq.idea.plugin.beancombiner.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Config {
    private String version;
    private List<Mapping> mapping;

    @Getter @Setter
    public static class Mapping {
        private String target;
        private List<Combine> combine;

        @Getter @Setter
        public static class Combine {
            private String from;
            private List<Field> fields;

            public void merge(List<Field> fields) {
                List<Field> configFields = this.fields;
                this.fields = new ArrayList<>();
                for (Field field : fields) {
                    for (Field configField : configFields) {
                        if(configField.getSource().equals(field.getSource())) {
                            field.setTarget(configField.getTarget());
                            field.setEnabled(true);
                            configFields.remove(configField);
                            break;
                        }
                    }
                    this.fields.add(field);
                }

            }

            @NoArgsConstructor
            @Getter @Setter @Accessors(chain = true)
            public static class Field {
                private String source;
                private String target;
                private boolean enabled;

                public Field(String str) {
                    String[] tokens = str.split(",");
                    source = tokens[0].trim();
                    if(tokens.length > 1) {
                        target = tokens[1].trim();
                    }else{
                        target = source;
                    }
                    enabled = true;
                }

                public Field(String source, String target) {
                    this.source = source;
                    this.target = target;

                    enabled = false;
                }
            }
        }
    }
}

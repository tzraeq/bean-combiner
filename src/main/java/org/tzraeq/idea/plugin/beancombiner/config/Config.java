package org.tzraeq.idea.plugin.beancombiner.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Config {
    public final static String DEFAULT_VERSION = "1";

    private String version;
    private List<Mapping> mapping;

    @Getter @Setter
    public static class Mapping {
        private String target;
        private List<Combine> combine;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Getter @Setter
        public static class Combine {
            private String from;
            private List<Field> fields;

            public void merge(List<Field> fields) {
                List<Field> configFields = this.fields;
                if(null == configFields) {
                    this.fields = fields;
                    for (Field field : fields) {
                        field.setEnabled(true);
                    }
                }else{
                    this.fields = new ArrayList<>();
                    for (Field field : fields) {
                        for (Field configField : configFields) {
                            if(field.getSource().equals(configField.getSource())) {
                                field.setTarget(configField.getTarget());
                                field.setEnabled(true);
                                configFields.remove(configField);
                                break;
                            }
                        }
                        this.fields.add(field);
                    }
                }
            }

            @JsonSerialize(using = FieldSerializer.class)
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

    public static class FieldSerializer extends JsonSerializer<Mapping.Combine.Field> {

        @Override
        public void serialize(Config.Mapping.Combine.Field value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if(value.getEnabled()){
                StringBuilder sb = new StringBuilder(value.getSource());
                if(!value.getSource().equals(value.getTarget())) {
                    sb.append("," + value.getTarget());
                }
                gen.writeString(sb.toString());
            }
        }
    }
}

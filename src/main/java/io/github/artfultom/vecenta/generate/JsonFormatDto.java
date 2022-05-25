package io.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class JsonFormatDto implements Serializable {

    @JsonProperty("client")
    private String client;

    @JsonProperty("entities")
    private List<Entity> entities;

    public JsonFormatDto() {
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public static class Entity implements Serializable {

        @JsonProperty("name")
        private String name;

        @JsonProperty("methods")
        private List<Method> methods;

        public Entity() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Method> getMethods() {
            return methods;
        }

        public void setMethods(List<Method> methods) {
            this.methods = methods;
        }

        public static class Method implements Serializable {

            @JsonProperty("name")
            private String name;

            @JsonProperty("http")
            private Http http;

            @JsonProperty("in")
            private List<Param> in;

            @JsonProperty("out")
            private List<Param> out;

            public Method() {
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Http getHttp() {
                return http;
            }

            public void setHttp(Http http) {
                this.http = http;
            }

            public List<Param> getIn() {
                return in;
            }

            public void setIn(List<Param> in) {
                this.in = in;
            }

            public List<Param> getOut() {
                return out;
            }

            public void setOut(List<Param> out) {
                this.out = out;
            }

            public static class Http implements Serializable {

                @JsonProperty("enabled")
                private Boolean enabled;

                @JsonProperty("cacheable")
                private Boolean cacheable;

                public Http() {
                }

                public Boolean getEnabled() {
                    return enabled;
                }

                public void setEnabled(Boolean enabled) {
                    this.enabled = enabled;
                }

                public Boolean getCacheable() {
                    return cacheable;
                }

                public void setCacheable(Boolean cacheable) {
                    this.cacheable = cacheable;
                }
            }

            public static class Param implements Serializable {

                @JsonProperty("name")
                private String name;

                @JsonProperty("type")
                private String type;

                public Param() {
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }
            }
        }
    }
}

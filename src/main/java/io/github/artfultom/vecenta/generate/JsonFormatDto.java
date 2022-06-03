package io.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
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

        @JsonProperty("models")
        private List<Model> models;

        public Entity() {
            this.methods = new ArrayList<>();
            this.models = new ArrayList<>();
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

        public List<Model> getModels() {
            return models;
        }

        public void setModels(List<Model> models) {
            this.models = models;
        }

        public static class Method implements Serializable {

            @JsonProperty("name")
            private String name;

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

        }

        public static class Model implements Serializable {

            @JsonProperty("name")
            private String name;

            @JsonProperty("fields")
            private List<Param> fields;

            public Model() {
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<Param> getFields() {
                return fields;
            }

            public void setFields(List<Param> fields) {
                this.fields = fields;
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

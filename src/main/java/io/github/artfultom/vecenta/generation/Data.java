package io.github.artfultom.vecenta.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class Data {

    private List<Client> clients;

    public Data() {
        this.clients = Collections.emptyList();
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public static class Client {
        @JsonProperty("name")
        private String name;

        @JsonProperty("entities")
        private List<Entity> entities;

        public Client() {
            this.entities = Collections.emptyList();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Entity> getEntities() {
            return entities;
        }

        public void setEntities(List<Entity> entities) {
            this.entities = entities;
        }
    }

    public static class Entity {

        @JsonProperty("name")
        private String name;

        @JsonProperty("methods")
        private List<Method> methods;

        @JsonProperty("models")
        private List<Model> models;

        public Entity() {
            this.methods = Collections.emptyList();
            this.models = Collections.emptyList();
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

        public static class Method {

            @JsonProperty("name")
            private String name;

            @JsonProperty("in")
            private List<Param> in;

            @JsonProperty("out")
            private String out;

            @JsonProperty("errors")
            private List<String> errors;

            public Method() {
                this.in = Collections.emptyList();
                this.errors = Collections.emptyList();
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

            public String getOut() {
                return out;
            }

            public void setOut(String out) {
                this.out = out;
            }

            public List<String> getErrors() {
                return errors;
            }

            public void setErrors(List<String> errors) {
                this.errors = errors;
            }
        }

        public static class Model {

            @JsonProperty("name")
            private String name;

            @JsonProperty("fields")
            private List<Param> fields;

            public Model() {
                this.fields = Collections.emptyList();
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

        public static class Param {

            @JsonProperty("name")
            private String name;

            @JsonProperty("type")
            private String type;

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

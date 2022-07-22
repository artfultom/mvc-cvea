package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.generated.v1.math.Model3;

import java.util.List;
import java.util.Map;

public class TestServerImpl implements TestServer {

    @Override
    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public String concat(String a, String b, String c) {
        return a + b + c;
    }

    @Override
    public Model3 echo(Model3 a) {
        return a;
    }

    @Override
    public List<Integer> echo(List<Integer> a) {
        return a;
    }

    @Override
    public List<Model3> echo(List<Model3> a, List<Model3> b) {
        a.addAll(b);

        return a;
    }

    @Override
    public Map<Integer, Model3> echo(Map<Integer, Model3> a) {
        return a;
    }

    @Override
    public Map<Integer, List<Model3>> echo(Map<Integer, List<Model3>> a, Map<Integer, List<Model3>> b) {
        a.putAll(b);

        return a;
    }

    @Override
    public Integer supply() {
        return 42;
    }

    @Override
    public void consume(Integer a) {
    }

    @Override
    public void error1() {

    }

    @Override
    public void error2() {

    }

    @Override
    public void error3() {

    }
}

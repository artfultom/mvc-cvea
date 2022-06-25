package io.github.artfultom.vecenta.generate;

import org.junit.Assert;
import org.junit.Test;

public class DefaultValidateStrategyTest {

    private final DefaultValidateStrategy strategy = new DefaultValidateStrategy();

    @Test
    public void isCorrectFileName() {
        Assert.assertTrue(strategy.isCorrect("Name.1.json"));
        Assert.assertFalse(strategy.isCorrect((String) null));
        Assert.assertFalse(strategy.isCorrect(""));
        Assert.assertFalse(strategy.isCorrect("Name.json"));
        Assert.assertFalse(strategy.isCorrect(".1.json"));
        Assert.assertFalse(strategy.isCorrect("Name..json"));
        Assert.assertFalse(strategy.isCorrect("Name.1."));
        Assert.assertFalse(strategy.isCorrect("Name.1.test"));
        Assert.assertFalse(strategy.isCorrect("Name.A.json"));
    }

    @Test
    public void isCorrectDto() {

    }

}
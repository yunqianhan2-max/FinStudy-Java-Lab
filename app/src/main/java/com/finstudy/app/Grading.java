package com.finstudy.app;


import java.util.Collection;
import java.util.TreeSet;

/** 与界面、数据库无关的纯 Java 判分规则，可在普通 JVM 上测试。 */
public final class Grading {
    private Grading() { }
    public static String encode(Collection<Integer> values) {
        TreeSet<Integer> sorted = new TreeSet<Integer>(values);
        StringBuilder result = new StringBuilder();
        for (Integer value : sorted) {
            if (result.length() > 0) result.append(',');
            result.append(value);
        }
        return result.toString();
    }
    public static boolean matches(String correctAnswer, String userAnswer) {
        return correctAnswer != null && !correctAnswer.isEmpty()
                && correctAnswer.equals(userAnswer);
    }
    public static int percentage(int correct, int total) {
        return total <= 0 ? 0 : Math.round(correct * 100f / total);
    }
}

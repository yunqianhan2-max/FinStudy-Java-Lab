import com.finstudy.app.Grading;
import java.util.Arrays;

public final class GradingTest {
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        check("0,2".equals(Grading.encode(Arrays.asList(2, 0))), "selection order must not change answer");
        check("0,2".equals(Grading.encode(Arrays.asList(2, 0, 2))), "duplicate selections must be removed");
        check(Grading.matches("0,2", "0,2"), "exact multi choice");
        check(!Grading.matches("0,2", "0"), "missing option is wrong");
        check(!Grading.matches("0,2", "0,1,2"), "extra option is wrong");
        check(!Grading.matches("0", ""), "unanswered is wrong");
        check(!Grading.matches("", ""), "empty correct answer is not valid");
        check(!Grading.matches(null, ""), "null correct answer is not valid");
        check(Grading.matches("1", "1"), "single choice");
        int correct = 0;
        for (int i = 0; i < 10; i++) if (Grading.matches("1", i < 8 ? "1" : "0")) correct++;
        check(correct * 10 == 80 && Grading.percentage(correct, 10) == 80, "8 of 10 gives 80 points and 80 percent");
        check(Grading.percentage(0, 0) == 0, "first use has no division by zero");
        check(Grading.percentage(15, 21) == 71, "weighted cumulative accuracy");
        System.out.println("PASS: 12 grading edge cases");
    }
}

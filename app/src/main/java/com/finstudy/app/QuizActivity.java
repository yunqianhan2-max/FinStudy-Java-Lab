package com.finstudy.app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;

/** XML 声明四个单选/多选项；Java 填充当前题目，答案独立保存。 */
public class QuizActivity extends BaseActivity implements View.OnClickListener {
    private Spinner chapterSpinner;
    private RadioGroup radioGroup;
    private final RadioButton[] radioButtons = new RadioButton[4];
    private final CheckBox[] checkBoxes = new CheckBox[4];
    private final ArrayList<TreeSet<Integer>> choices = new ArrayList<TreeSet<Integer>>();
    private ArrayList<JSONObject> questions = new ArrayList<JSONObject>();
    private ArrayList<JSONObject> chapters;
    private int chapterId = 1;
    private int index;
    private boolean started, review, binding, submitting;
    private String questionId;
    private String token = UUID.randomUUID().toString();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_quiz, "章节测验");
        chapterSpinner = findViewById(R.id.sp_quiz_chapter);
        radioGroup = findViewById(R.id.rg_answers);
        chapters = repo.availableChapters(true);
        ArrayList<String> titles = new ArrayList<String>();
        for (JSONObject chapter : chapters) titles.add(chapter.optString("title"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chapterSpinner.setAdapter(adapter);
        int[] radioIds = {R.id.rb_answer_0, R.id.rb_answer_1, R.id.rb_answer_2, R.id.rb_answer_3};
        int[] checkIds = {R.id.cb_answer_0, R.id.cb_answer_1, R.id.cb_answer_2, R.id.cb_answer_3};
        for (int i = 0; i < 4; i++) {
            radioButtons[i] = findViewById(radioIds[i]);
            checkBoxes[i] = findViewById(checkIds[i]);
            final int option = i;
            checkBoxes[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton button, boolean checked) {
                    if (binding || !started) return;
                    if (checked) choices.get(index).add(option); else choices.get(index).remove(option);
                }
            });
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (binding || !started) return;
                choices.get(index).clear();
                for (int i = 0; i < radioButtons.length; i++) {
                    if (radioButtons[i].getId() == checkedId) choices.get(index).add(i);
                }
            }
        });
        int[] buttons = {R.id.btn_start_quiz, R.id.btn_previous_question,
                R.id.btn_next_question, R.id.btn_submit_quiz};
        for (int id : buttons) findViewById(id).setOnClickListener(this);
        chapterId = getIntent().getIntExtra("chapter_id", 1);
        questionId = getIntent().getStringExtra("question_id");
        review = questionId != null;
        if (state != null) {
            chapterId = state.getInt("chapter", chapterId);
            index = state.getInt("index", 0);
            token = state.getString("token", token);
            started = state.getBoolean("started", false);
        }
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).optInt("chapterId") == chapterId) chapterSpinner.setSelection(i);
        }
        if (review || started) {
            if (!loadQuestions()) return;
            if (state != null) {
                for (int i = 0; i < choices.size(); i++) {
                    String value = state.getString("answer_" + i, "");
                    if (!value.isEmpty()) {
                        for (String part : value.split(",")) choices.get(i).add(Integer.parseInt(part));
                    }
                }
            }
            showQuestion();
        } else if (chapters.isEmpty()) {
            findViewById(R.id.btn_start_quiz).setEnabled(false);
            ((TextView) findViewById(R.id.tv_quiz_rules)).setText("暂无可用题库，请检查课程文件。");
        }
    }
    private boolean loadQuestions() {
        questions.clear(); choices.clear();
        if (review) {
            JSONObject question = repo.find(repo.questions, "questionId", questionId);
            if (question != null) { questions.add(question); chapterId = question.optInt("chapterId"); }
        } else questions = repo.items(repo.questions, chapterId);
        if (!repo.available(chapterId) || questions.isEmpty()) {
            toast("题目或章节不存在"); finish(); return false;
        }
        for (int i = 0; i < questions.size(); i++) choices.add(new TreeSet<Integer>());
        started = true;
        index = Math.max(0, Math.min(index, questions.size() - 1));
        return true;
    }
    private void showQuestion() {
        binding = true;
        findViewById(R.id.quiz_area).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_start_quiz).setVisibility(View.GONE);
        chapterSpinner.setEnabled(false);
        if (review) chapterSpinner.setVisibility(View.GONE);
        JSONObject question = questions.get(index);
        boolean single = "single".equals(question.optString("type"));
        ((TextView) findViewById(R.id.tv_question_position)).setText(repo.chapter(chapterId) + "\n"
                + (index + 1) + " / " + questions.size() + (single ? " · 单选" : " · 多选"));
        ((TextView) findViewById(R.id.tv_question_stem)).setText(question.optString("stem"));
        radioGroup.clearCheck();
        JSONArray options = question.optJSONArray("options");
        for (int i = 0; i < 4; i++) {
            String label = (char) ('A' + i) + ". " + options.optString(i);
            radioButtons[i].setText(label);
            checkBoxes[i].setText(label);
            checkBoxes[i].setChecked(choices.get(index).contains(i));
            if (single && choices.get(index).contains(i)) radioGroup.check(radioButtons[i].getId());
        }
        radioGroup.setVisibility(single ? View.VISIBLE : View.GONE);
        findViewById(R.id.multi_answers).setVisibility(single ? View.GONE : View.VISIBLE);
        findViewById(R.id.btn_previous_question).setEnabled(index > 0);
        findViewById(R.id.btn_next_question).setEnabled(index + 1 < questions.size());
        binding = false;
    }
    @Override public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_start_quiz) {
            if (chapters.isEmpty()) return;
            chapterId = chapters.get(chapterSpinner.getSelectedItemPosition()).optInt("chapterId");
            index = 0;
            if (loadQuestions()) showQuestion();
        } else if (id == R.id.btn_previous_question && index > 0) {
            index--; showQuestion();
        } else if (id == R.id.btn_next_question && index + 1 < questions.size()) {
            index++; showQuestion();
        } else if (id == R.id.btn_submit_quiz) confirmSubmit();
    }
    private void confirmSubmit() {
        int empty = 0;
        for (TreeSet<Integer> answer : choices) if (answer.isEmpty()) empty++;
        String message = empty == 0 ? "提交后保存成绩和每题作答记录。"
                : "还有 " + empty + " 道题未作答，将按错误处理。";
        new AlertDialog.Builder(this).setTitle("确认提交").setMessage(message)
                .setNegativeButton("继续答题", null)
                .setPositiveButton("提交", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { submit(); }
                }).show();
    }
    private void submit() {
        if (submitting) return;
        submitting = true;
        findViewById(R.id.btn_submit_quiz).setEnabled(false);
        ArrayList<String> answers = new ArrayList<String>();
        for (TreeSet<Integer> answer : choices) answers.add(Grading.encode(answer));
        try {
            long attemptId = store.submit(token, chapterId, questions, answers, review);
            Bundle bundle = new Bundle(); bundle.putLong("attempt_id", attemptId);
            openPage(QuizResultActivity.class, bundle);
            finish();
        } catch (Exception e) {
            submitting = false;
            findViewById(R.id.btn_submit_quiz).setEnabled(true);
            toast("保存失败，请重试");
            android.util.Log.e("FinStudy", "保存测验失败", e);
        }
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        int selectedChapter = chapterId;
        if (!started && !chapters.isEmpty()) selectedChapter = chapters.get(chapterSpinner.getSelectedItemPosition()).optInt("chapterId");
        state.putInt("chapter", selectedChapter);
        state.putInt("index", index);
        state.putString("token", token);
        state.putBoolean("started", started);
        for (int i = 0; i < choices.size(); i++) state.putString("answer_" + i, Grading.encode(choices.get(i)));
    }
}

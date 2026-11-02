package com.finstudy.app;


import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/** assets 只保存课程内容，SQLite 只保存用户学习记录；原有 ID 保持稳定。 */
public class Repo {
    public JSONArray chapters = new JSONArray();
    public JSONArray knowledge = new JSONArray();
    public JSONArray questions = new JSONArray();
    public String error;

    public Repo(Context context) {
        try {
            chapters = load(context, "chapters.json");
            knowledge = load(context, "knowledge.json");
            questions = load(context, "questions.json");
            validate();
        } catch (Exception e) {
            error = e.getMessage();
            chapters = new JSONArray(); knowledge = new JSONArray(); questions = new JSONArray();
        }
    }
    private JSONArray load(Context context, String filename) throws Exception {
        try (InputStream input = context.getAssets().open(filename);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return new JSONArray(output.toString("UTF-8"));
        }
    }
    private void validate() throws JSONException {
        Set<Integer> chapterIds = new HashSet<Integer>();
        for (JSONObject chapter : items(chapters, 0)) {
            int id = chapter.getInt("chapterId");
            if (id <= 0 || !chapterIds.add(id)) throw new JSONException("章节编号重复或无效");
        }
        Set<String> knowledgeIds = new HashSet<String>();
        for (JSONObject item : items(knowledge, 0)) {
            if (!knowledgeIds.add(item.getString("knowledgeId"))
                    || !chapterIds.contains(item.getInt("chapterId"))) {
                throw new JSONException("知识点 ID 或章节归属无效");
            }
        }
        Set<String> questionIds = new HashSet<String>();
        for (JSONObject question : items(questions, 0)) {
            JSONArray options = question.getJSONArray("options");
            JSONArray correct = question.getJSONArray("correctAnswers");
            String type = question.getString("type");
            if (!questionIds.add(question.getString("questionId"))
                    || !chapterIds.contains(question.getInt("chapterId"))
                    || options.length() != 4 || correct.length() == 0
                    || !("single".equals(type) || "multiple".equals(type))
                    || ("single".equals(type) && correct.length() != 1)
                    || ("multiple".equals(type) && correct.length() < 2)) {
                throw new JSONException("题库结构无效");
            }
            Set<Integer> selected = new HashSet<Integer>();
            for (int i = 0; i < correct.length(); i++) {
                int value = correct.getInt(i);
                if (value < 0 || value >= options.length() || !selected.add(value)) {
                    throw new JSONException("题目标准答案无效");
                }
            }
            if (question.getString("explanation").trim().isEmpty()) {
                throw new JSONException("题目缺少解析");
            }
        }
    }
    public ArrayList<JSONObject> items(JSONArray array, int chapterId) {
        ArrayList<JSONObject> result = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null && (chapterId == 0 || item.optInt("chapterId") == chapterId)) result.add(item);
        }
        return result;
    }
    public ArrayList<JSONObject> availableChapters(boolean requireQuestions) {
        ArrayList<JSONObject> result = new ArrayList<JSONObject>();
        for (JSONObject chapter : items(chapters, 0)) {
            int id = chapter.optInt("chapterId");
            if (chapter.optBoolean("available") && !items(knowledge, id).isEmpty()
                    && (!requireQuestions || !items(questions, id).isEmpty())) result.add(chapter);
        }
        return result;
    }
    public boolean available(int chapterId) {
        for (JSONObject chapter : availableChapters(false)) {
            if (chapter.optInt("chapterId") == chapterId) return true;
        }
        return false;
    }
    public JSONObject find(JSONArray array, String key, String id) {
        for (JSONObject item : items(array, 0)) {
            if (item.optString(key).equals(id)) return item;
        }
        return null;
    }
    public String chapter(int chapterId) {
        JSONObject chapter = find(chapters, "chapterId", String.valueOf(chapterId));
        return chapter == null ? "章节不存在" : chapter.optString("title");
    }
    public static String answer(JSONArray array) {
        TreeSet<Integer> values = new TreeSet<Integer>();
        for (int i = 0; i < array.length(); i++) values.add(array.optInt(i));
        return Grading.encode(values);
    }
    public static String labels(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "未作答";
        StringBuilder result = new StringBuilder();
        for (String value : encoded.split(",")) {
            if (result.length() > 0) result.append(' ');
            result.append((char) ('A' + Integer.parseInt(value)));
        }
        return result.toString();
    }
}

package com.finstudy.app;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** 先阅读 XML/Java，点击运行后才创建示例控件，并调用对应 Java 方法。 */
public class LabActivity extends BaseActivity implements View.OnClickListener {
    private LabExample[] examples;
    private int selectedIndex;
    private boolean showingJava;
    private boolean running;
    private String xmlSource = "";
    private String javaSource = "";
    private String sourceError;
    private TextView sourceView;
    private TextView emptyPreview;
    private FrameLayout preview;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_lab, "代码与效果实验室");
        examples = LabCatalog.create();
        sourceView = findViewById(R.id.tv_lab_source);
        preview = findViewById(R.id.lab_preview);
        emptyPreview = findViewById(R.id.tv_lab_preview_empty);
        sourceView.setMovementMethod(new ScrollingMovementMethod());
        // 在代码区域滑动时先滚动代码，手指离开后恢复整个页面滚动。
        sourceView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent event) {
                boolean touching = event.getActionMasked() != MotionEvent.ACTION_UP
                        && event.getActionMasked() != MotionEvent.ACTION_CANCEL;
                view.getParent().requestDisallowInterceptTouchEvent(touching);
                return false;
            }
        });
        if (state != null) {
            String key = state.getString("lab_example", "text");
            for (int i = 0; i < examples.length; i++) {
                if (examples[i].key.equals(key)) selectedIndex = i;
            }
            showingJava = state.getBoolean("lab_java", false);
        }
        Spinner spinner = findViewById(R.id.sp_lab_example);
        String[] titles = new String[examples.length];
        for (int i = 0; i < examples.length; i++) titles[i] = examples[i].title;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedIndex != position) {
                    selectedIndex = position;
                    selectExample();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        RadioGroup codeType = findViewById(R.id.rg_lab_code_type);
        codeType.check(showingJava ? R.id.rb_lab_java : R.id.rb_lab_xml);
        codeType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                showingJava = checkedId == R.id.rb_lab_java;
                showCode();
            }
        });
        int[] buttons = {R.id.btn_lab_run, R.id.btn_lab_reset,
                R.id.btn_lab_full_code, R.id.btn_lab_navigation};
        for (int id : buttons) findViewById(id).setOnClickListener(this);
        selectExample();
        if (state != null && state.getBoolean("lab_running", false)) runExample(false);
    }

    private void selectExample() {
        LabExample example = examples[selectedIndex];
        ((TextView) findViewById(R.id.tv_lab_goal)).setText(example.goal);
        ((TextView) findViewById(R.id.tv_lab_prediction)).setText(example.prediction);
        ((TextView) findViewById(R.id.tv_lab_try)).setText("操作提示：" + example.tryText);
        sourceError = null;
        try {
            xmlSource = readSource(example.xmlAsset);
            javaSource = readSource(example.javaAsset);
        } catch (Exception e) {
            sourceError = "示例源码读取失败，请检查安装包中的 lab 资源。";
            xmlSource = sourceError;
            javaSource = sourceError;
            Log.e("FinStudy", "读取实验源代码失败", e);
        }
        showCode();
        running = false;
        preview.removeAllViews();
        emptyPreview.setVisibility(View.VISIBLE);
        preview.addView(emptyPreview);
        ((TextView) findViewById(R.id.tv_lab_run_status)).setText("尚未运行。阅读代码后点击“运行这段示例”。");
        findViewById(R.id.btn_lab_run).setEnabled(sourceError == null);
        findViewById(R.id.btn_lab_reset).setEnabled(false);
    }

    private String readSource(String path) throws Exception {
        try (InputStream input = getAssets().open(path);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1) output.write(buffer, 0, length);
            return output.toString("UTF-8");
        }
    }

    private void showCode() {
        LabExample example = examples[selectedIndex];
        String path = showingJava ? example.javaAsset : example.xmlAsset;
        ((TextView) findViewById(R.id.tv_lab_source_file)).setText(path + "  · 可上下滑动或展开查看");
        sourceView.setText(showingJava ? javaSource : xmlSource);
        sourceView.scrollTo(0, 0);
    }

    private void runExample(boolean scrollToPreview) {
        if (sourceError != null) { toast(sourceError); return; }
        final LabExample example = examples[selectedIndex];
        try {
            // 这两句是“运行”的关键：加载示例布局，再执行同一个 Java 示例。
            View demoView = getLayoutInflater().inflate(example.layoutId, preview, false);
            example.binding.bind(demoView);
            preview.removeAllViews();
            emptyPreview.setVisibility(View.GONE);
            preview.addView(emptyPreview);
            preview.addView(demoView);
            running = true;
            findViewById(R.id.btn_lab_reset).setEnabled(true);
            ((TextView) findViewById(R.id.tv_lab_run_status)).setText("已运行：" + example.title
                    + "\n下方是真实控件，可以继续点击或拖动。");
            if (scrollToPreview) {
                final ScrollView page = findViewById(R.id.lab_page_scroll);
                page.post(new Runnable() {
                    @Override public void run() {
                        page.smoothScrollTo(0, findViewById(R.id.lab_preview_heading).getTop());
                    }
                });
            }
        } catch (RuntimeException e) {
            running = false;
            preview.removeAllViews();
            emptyPreview.setVisibility(View.VISIBLE);
            preview.addView(emptyPreview);
            findViewById(R.id.btn_lab_reset).setEnabled(false);
            ((TextView) findViewById(R.id.tv_lab_run_status)).setText("本次运行失败，请重新运行或切换示例。");
            Log.e("FinStudy", "运行示例失败：" + example.key, e);
        }
    }

    private void expandCode() {
        final String code = showingJava ? javaSource : xmlSource;
        ScrollView vertical = new ScrollView(this);
        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        TextView text = new TextView(this);
        text.setText(code);
        text.setTextSize(13);
        text.setTypeface(android.graphics.Typeface.MONOSPACE);
        text.setTextIsSelectable(true);
        int padding = Math.round(16 * getResources().getDisplayMetrics().density);
        text.setPadding(padding, padding, padding, padding);
        horizontal.addView(text);
        vertical.addView(horizontal);
        new AlertDialog.Builder(this).setTitle(showingJava ? "完整 Java 示例" : "完整 XML 示例")
                .setView(vertical).setPositiveButton("关闭", null)
                .setNeutralButton("复制代码", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("FinStudy 示例", code));
                        toast("代码已复制");
                    }
                }).show();
    }

    @Override public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_lab_run || id == R.id.btn_lab_reset) runExample(true);
        else if (id == R.id.btn_lab_full_code) expandCode();
        else if (id == R.id.btn_lab_navigation) openPage(NavigationActivity.class);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("lab_example", examples[selectedIndex].key);
        state.putBoolean("lab_java", showingJava);
        state.putBoolean("lab_running", running);
    }
}

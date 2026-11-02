package com.finstudy.app;

/** 每个条目只引用自己的 XML 与 Java，预览和展示代码一一对应。 */
public final class LabCatalog {
    private LabCatalog() { }
    public static LabExample[] create() {
        return new LabExample[] {
            new LabExample("text", "TextView：文字、字号与颜色", "观察 XML 默认文字被 Java 的 setText、setTextSize 和 setTextColor 改变。", "先猜一猜：运行后文字会变成什么？字号和颜色会怎样？", "运行后应看到 24sp 的玫红色“欢迎学习 Android！”。切换 XML 和 Java，找出三处变化的原因。", "c4k1",
                    R.layout.demo_lab_text, new DemoText()),
            new LabExample("layout", "LinearLayout：横排与竖排", "用 orientation 控制同一组子控件横向或纵向排列。", "XML 中 orientation=\"horizontal\"，控件 A、B 初始会如何摆放？", "点击预览中的“切换为纵向排列”，再点一次切回横向。对照 Java 的 setOrientation。", "c3k3",
                    R.layout.demo_lab_layout, new DemoLayout()),
            new LabExample("button", "Button：点击后修改文字", "点击事件进入 onClick，然后通过 TextView.setText 更新界面。", "运行只会显示按钮；还需要点击预览中的“开始学习”，才会执行 onClick。", "点击“开始学习”，文字应从“等待按钮点击”变成“欢迎学习 Android！”。重新运行可恢复。", "c4k2",
                    R.layout.demo_lab_button, new DemoButton()),
            new LabExample("radio", "RadioGroup：同组只能选一个", "RadioGroup 中的两个 RadioButton 互斥，监听器读取当前选项。", "尝试先选“课前预习”再选“课后复习”，前一个会不会仍然选中？", "在两个选项之间切换，观察选中状态和下方文字。两个按钮放在同一 RadioGroup 内。", "c4k3",
                    R.layout.demo_lab_radio, new DemoRadio()),
            new LabExample("check", "CheckBox：同时选择多个", "每个 CheckBox 独立保存状态，用 isChecked 组合选中的文字。", "Java 和 XML 可以同时选中吗？取消一个后输出会如何变化？", "任选多个选项，再取消其中一个；观察“已选”内容随复选状态更新。", "c4k4",
                    R.layout.demo_lab_check, new DemoCheck()),
            new LabExample("seek", "SeekBar：拖动改变图片透明度", "SeekBar 的 0–100 进度除以 100f，映射到图片的 0–1 alpha。", "把滑块移到 50，progress / 100f 的值是多少？", "依次拖到 100、50、0，观察图片不透明、半透明、完全透明的效果及数值。", "c4k5",
                    R.layout.demo_lab_seek, new DemoSeek()),
            new LabExample("rating", "RatingBar：五颗星与半星评分", "XML 的 numStars、stepSize 定义评分范围与精度，Java 读取浮点星级。", "stepSize=\"0.5\" 时，能否给出 3.5 星？", "触摸或拖动星星，尝试选 3.5 星，观察下方评分。", "c4k6",
                    R.layout.demo_lab_rating, new DemoRating()),
            new LabExample("spinner", "Spinner：下拉选择与适配器", "ArrayAdapter 把三个字符串连接到 Spinner，选择回调更新文字。", "Java 中 topics 数组的三项，会显示到哪个控件里？", "点开下拉框选择“XML 布局”，观察已选内容。初次运行通常自动选中第一项。", "c4k8",
                    R.layout.demo_lab_spinner, new DemoSpinner()),
            new LabExample("list", "ListView：列表与点击事件", "同一个数组通过 ArrayAdapter 变成纵向列表，点击行后读取对应文字。", "点击列表的第三项，输出会是哪一个控件名称？", "分别点击 TextView、SeekBar 等条目，观察下方“点击了”文字。", "c4k8",
                    R.layout.demo_lab_list, new DemoList()),
            new LabExample("grid", "GridView：三列网格", "GridView 用 numColumns=\"3\" 将六项内容排列成三列网格。", "六个条目，每行三列，一共会显示几行？", "观察三列两行布局，点击任意条目，对照 OnItemClickListener 的输出。", "c4k7",
                    R.layout.demo_lab_grid, new DemoGrid())
        };
    }
}

package com.finstudy.app;


/** 一个固定课程示例的元数据和真实 Java 实现。 */
public final class LabExample {
    public final String key;
    public final String title;
    public final String goal;
    public final String prediction;
    public final String tryText;
    public final String knowledgeId;
    public final String xmlAsset;
    public final String javaAsset;
    public final int layoutId;
    public final LabExampleBinding binding;
    public LabExample(String key, String title, String goal, String prediction, String tryText,
                      String knowledgeId, int layoutId, LabExampleBinding binding) {
        this.key = key;
        this.title = title;
        this.goal = goal;
        this.prediction = prediction;
        this.tryText = tryText;
        this.knowledgeId = knowledgeId;
        this.xmlAsset = "lab/" + key + ".xml";
        this.javaAsset = "lab/" + key + ".java";
        this.layoutId = layoutId;
        this.binding = binding;
    }
}

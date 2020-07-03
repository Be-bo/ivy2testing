package com.ivy2testing.terms;

public class LongTextModel {

    private String title;
    private String text;

    public LongTextModel(String initialTitle, String initialText){
        title = initialTitle;
        text = initialText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

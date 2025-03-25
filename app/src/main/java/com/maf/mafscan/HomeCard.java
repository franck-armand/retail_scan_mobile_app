package com.maf.mafscan;

import java.util.List;

public class HomeCard {
    private final String fadedTitle;
    private final List<String> buttonTitles;
    private final List<Integer> buttonIcons;

    public HomeCard(String fadedTitle, List<String> buttonTitles, List<Integer> buttonIcons) {
        this.fadedTitle = fadedTitle;
        this.buttonTitles = buttonTitles;
        this.buttonIcons = buttonIcons;
    }

    public String getFadedTitle() {
        return fadedTitle;
    }

    public List<String> getButtonTitles() {
        return buttonTitles;
    }
    public List<Integer> getButtonIcons() {
        return buttonIcons;
    }
}
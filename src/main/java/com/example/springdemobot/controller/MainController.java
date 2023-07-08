package com.example.springdemobot.controller;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainController {

    public static String checkKeywords(String keyword1, String keyword2) {
        String url = "https://announcements.bybit.com/en-US/?category=new_crypto&page=1";
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при получении страницы";
        }

        Elements elements = document.select("span");
        StringBuilder pageContent = new StringBuilder();
        for (Element element : elements) {
            pageContent.append(element.ownText()).append(" ");
        }
        String page = pageContent.toString();
        int index1 = page.indexOf(keyword1);
        int index2 = page.indexOf(keyword2, index1 + keyword1.length());

        if (index1 != -1) {
            String link1 = url + "#position-" + index1;
            return "bybit содержит " + link1 + " " + keyword1;
        }

        if (index2 != -1) {
            String link2 = url + "#position-" + index2;
            return "bybit " + link2 + " " + keyword2;
        }

        return "bybit не содержит ни одного из ключевых слов";
    }
}
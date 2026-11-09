package io.github.javiewer.adapter.item;

import java.util.List;

/**
 * Project: JAViewer
 */

public class DataSource extends Linkable {

    public static DataSource AVMO = new DataSource("AVMOO 日本", "http://avmoo.shop/jav/data/api/");
    public static DataSource AVSO = new DataSource("AVMOO 日本无码", "http://avsox.click/javu/data/api/");
    public static DataSource AVXO = new DataSource("AVMOO 欧美", "http://avheat.shop/wav/data/api/");


    public String name;
    public List<String> legacies;

    public DataSource(String name, String baseUrl) {
        this.name = name;
        this.link = baseUrl;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}

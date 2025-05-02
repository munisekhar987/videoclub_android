package com.videoclub.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class for menu items
 */
public class MenuItem {
    private String id;
    private String name;
    private String image;
    private String webUrl;

    public MenuItem() {
    }

    public MenuItem(String id, String name, String image, String webUrl) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.webUrl = webUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("image", image);
            jsonObject.put("web_url", webUrl);
            return jsonObject.toString();
        } catch (JSONException e) {
            return "MenuItem{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", image='" + image + '\'' +
                    ", webUrl='" + webUrl + '\'' +
                    '}';
        }
    }

    /**
     * Create MenuItem from JSON string
     */
    public static MenuItem fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            MenuItem menuItem = new MenuItem();
            menuItem.setId(jsonObject.optString("id"));
            menuItem.setName(jsonObject.optString("name"));
            menuItem.setImage(jsonObject.optString("image"));
            menuItem.setWebUrl(jsonObject.optString("web_url"));
            return menuItem;
        } catch (JSONException e) {
            return null;
        }
    }
}
package pubgm.loader.ifc;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class User {
    private String id;
    private String username;
    private String userKey;
    private String imageURL;

    public User(String id, String username, String userKey, String imageURL) {
        this.id = id;
        this.username = username;
        this.userKey = userKey;
        this.imageURL = imageURL;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    
    public boolean isPremium() {
        return userKey.equals("");
    }
}

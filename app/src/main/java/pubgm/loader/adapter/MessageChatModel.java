package pubgm.loader.adapter;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class MessageChatModel {
    private String uid;
    private String user;
    private String msg;
    private String time;

    public MessageChatModel(String uid, String user, String msg, String time) {
        this.uid = uid;
        this.user = user;
        this.msg = msg;
        this.time = time;
    }
    
    public MessageChatModel() {
    }
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }

    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
}

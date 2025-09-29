package pubgm.loader.ifc;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class Game {
    private String title;
    private String pkg;
    private String status;
    private String version;
    private int id;

    public Game(String title, String pkg, String status, String version, int id) {
        this.title = title;
        this.pkg = pkg;
        this.status = status;
        this.version = version;
        this.id = id;
    }
    
    public Game() {
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPackage() {
        return pkg;
    }
    
    public void setPackage(String pkg) {
        this.pkg = pkg;
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}

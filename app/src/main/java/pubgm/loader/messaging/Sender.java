package pubgm.loader.messaging;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class Sender {
    public Data data;
    public String to;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
}
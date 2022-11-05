package ac.jnu.flowbot.data.database;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

public class HrefInfo implements Serializable, Comparable<HrefInfo>{

    @Serial
    private static final long serialVersionUID = 100L;

    String title;
    String date;
    String link;

    public HrefInfo(String a, String b, String c) {
        title = a;
        date = b;
        link = c;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLink() {
        return link;
    }

    @Override
    public int compareTo(@NotNull HrefInfo o) {
        return link.compareTo(o.link);
    }
}

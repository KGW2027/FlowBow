package ac.jnu.flowbot.data;

import ac.jnu.flowbot.data.database.HrefInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RequestSerializer{

    public enum RequestSiteType {
        SOFTWARE("./datas/software.dat"),
        SOJUNG("./datas/sojung.dat"),
        ENGINEERING("./datas/engineering.dat"),
        ;

        String path;
        RequestSiteType(String p) {
            path = p;
        }
    }

    public List<HrefInfo> update(RequestSiteType type, List<HrefInfo> target) throws IOException, ClassNotFoundException {
        File file = new File(type.path);
        newFile(file);

        if (file.length() == 0) {
            updateFile(type, target);
            return target;
        }

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        List<HrefInfo> result = (List<HrefInfo>) ois.readObject();
        ois.close();

        List<HrefInfo> newList = new ArrayList<>();
        List<String> links = new ArrayList<>();
        for(HrefInfo res : result) links.add(res.getLink());

        for (HrefInfo t : target) if(!links.contains(t.getLink())) newList.add(t);

        updateFile(type, target);
        return newList;
    }

    private void updateFile(RequestSiteType type, List<HrefInfo> target) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(type.path));
        oos.writeObject(target);
        oos.close();
    }

    private void newFile(File file) throws IOException {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(!file.exists()) file.createNewFile();
    }
}

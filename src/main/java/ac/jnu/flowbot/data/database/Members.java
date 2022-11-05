package ac.jnu.flowbot.data.database;

import java.io.*;
import java.util.HashMap;

public class Members {

    private HashMap<Long, MemberData> members;
    private String membersString;
    private File path;

    protected Members() throws IOException {
        path = new File("./datas/members.dat");
        path.getParentFile().mkdirs();
        members = new HashMap<>();
        membersString = "";
        if(!path.exists()){
            path.createNewFile();
            update();
        }
        read();
    }

    protected void addNew(MemberData md) throws IOException {
        members.put(md.userId, md);
        update();
    }

    protected MemberData request(long id) {
        if(members.containsKey(id)) return members.get(id);
        return null;
    }

    private void update() throws IOException {
        StringBuilder builder = new StringBuilder().append("{");
        for(MemberData md : members.values()) {
            builder.append(String.format("""
                    {%d, %s, %d, %s, %s},
                    """, md.userId, md.name, Integer.parseInt(md.id.substring(0, 2)), md.college.toString(), md.registerDate));
        }
        builder.append("}");
        membersString = builder.toString();

        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        bw.write(membersString);
        bw.flush();
        bw.close();
    }

    private void read() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) builder.append(line);
        br.close();
        membersString = builder.toString();
        if(membersString.equals("{}")) return;
        String[] test = membersString.substring(1, membersString.length()-1).split(",");
        for(String info : test) {
            if(info.startsWith("{") && info.endsWith("}")) {
                String[] data = info.replace("{", "").replace("}", "").split(", ");
                MemberData md = new MemberData();
                md.userId = Long.parseLong(data[0]);
                md.name = data[1];
                md.id = data[2];
                md.college = College.Colleges.valueOf(data[3]);
                md.registerDate = data[4];
                members.put(md.userId, md);
            }
        }
    }



}

package ac.jnu.flowbot.data.database;

import java.io.IOException;

public class DatabaseIntegration {

    private static DatabaseIntegration instance;

    public static DatabaseIntegration getInstance() {
        if(instance == null) {
            try {
                instance = new DatabaseIntegration();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private final Members memberDB;

    private DatabaseIntegration() throws IOException {
        memberDB = new Members();
    }

    public void insertMemberData(MemberData md) throws IOException {
        memberDB.addNew(md);
    }

    public MemberData getMemberData(long id) {
        return memberDB.request(id);
    }
}

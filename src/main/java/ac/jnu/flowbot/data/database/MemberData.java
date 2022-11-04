package ac.jnu.flowbot.data.database;

import java.util.List;

/**
 * 데이터베이스 데이터
 */
public class MemberData {

    public Long userId;
    public String name;
    public String id;
    public College.Colleges college;
    public List<Languages> languages;
    public String registerDate;

}

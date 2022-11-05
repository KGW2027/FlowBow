package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.database.Languages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class FavoriteLanguages {

    public static final long channel = 1038366056709890148L;

    public static MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(new Color(0xFFAA37));

        builder.setTitle("선호하는 프로그래밍 언어를 선택해주세요.");
        builder.setDescription(getDescription());

        return builder.build();
    }

    private static String getDescription() {
        String text = """
                굳이 해본 적 없는 언어라도, 관심이 있는 언어라면 선택해보세요!
                언어와 함께 간략한 소개도 적어드릴게요.
                
                %JAVA%  [JAVA] Android 개발이나 Spring Framework를 통한 백엔드
                %CSHARP%  [C#] WPF를 이용한 GUI 프로그래밍, Unity
                %CPP%  [C++] 고성능을 위한 Embeded, Game, Iot, Unreal
                %REACT%  [Javascript] React, Vue, Angular를 이용한 프론트엔드
                %LANGDART%  [Dart] Flutter를 이용한 모바일 통합 개발
                %PYTHON%  [Python] 입문용 언어로 유명함, 인공지능, 딥러닝
                %LANGC%  [C] 최고의 성능을 위한 Embedded, OS, Language
                %RUST%  [Rust] 메모리 안전성이 보장되는 언어
                %TYPESCRIPT%  [TypeScript] 정적타입을 지원하는 Javascript
                %ASSEMBLY%  [Assembly] 어셈블리어
                %SWIFT%  [Swift] IOS 환경을 위한 프로그래밍 언어
                %MATLAB%  [MATLAB] 수학
                %LANGR%  [R] 통계
                %UNITY%  [UNITY] C#을 이용한 게임 개발
                %UNREAL%  [UNREAL] C++를 이용한 비주얼 개발
                
                채팅에 달린 이모지를 누르시면 해당 언어 역할을 지급해드릴게요!
                만약, 이모지가 보이지 않는다면 '설정 > 텍스트 및 사진 > 메세지에 이모티콘 반응 표시하기'를 켜주세요.
                """;

        for(Languages lang : Languages.values())
            text = text.replace(String.format("%%%s%%", lang.name()), lang.getEmoji());
        return text;
    }
}

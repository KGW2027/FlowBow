package ac.jnu.flowbot.data.database;

import java.util.Locale;

public enum Languages {

    JAVA(1038366929741688894L),
    CSHARP(1038367766916382750L),
    CPP(1038367547126464552L),
    REACT(1038367485977702451L),
    LANGDART(1038368065152356422L),
    PYTHON(1038368271747010560L),
    LANGC(1038368370631905330L),
    RUST(1038368472020815902L),
    TYPESCRIPT(1038368926297501736L),
    ASSEMBLY(1038369193478856785L),
    SWIFT(1038369578532749372L),
    UNITY(1038370937952817163L),
    UNREAL(1038370939576000522L),
    MATLAB(1038370008805429278L),
    LANGR(1038370331548717077L);

    long id;

    Languages(long id) {
        this.id = id;
    }

    public String getEmoji() {
        return String.format("<:%s:%d>", name().toLowerCase(Locale.ROOT), id);
    }

    public long getId() {
        return id;
    }

    public static Languages getEnumByLong(long l) {
        for(Languages lang : values()) if(lang.id == l) return lang;
        return null;
    }

}

package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.RequestSerializer;
import ac.jnu.flowbot.data.database.HrefInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebHTTPRequester implements Runnable{


    private final String software = "https://sw.jnu.ac.kr";
    private final long softwareTC = 1038409177887932416L;
    private final String sojung = "https://sojoong.kr/www/notice/";

    private final Pattern swHrefPattern;
    private final Pattern swStrongPattern;
    private final Pattern swDatePattern;

    private final Pattern sjHrefPattern;
    private final Pattern sjStrongPattern;
    private final Pattern sjDatePattern;

    private final long HOURS = 60 * 60 * 1000;


    public WebHTTPRequester() {
        this.swHrefPattern = Pattern.compile("(?=(<a href))(.*?)(?<=do)");
        this.swStrongPattern = Pattern.compile("(?<=(<strong>))(.*?)(?=</strong>)");
        this.swDatePattern = Pattern.compile("(?<=(\"td-date\">))(.*?)(?=</td>)");

        this.sjHrefPattern = Pattern.compile("(?<=(movePageView\\())(.*?)(?=\\))");
        this.sjStrongPattern = Pattern.compile("(?<=(\"cutText\">))(.*?)(?=</strong>)");
        this.sjDatePattern = Pattern.compile("(?<=(<em>))(.*?)(?=</em>)");
    }

    @Override
    public void run() {
        try {
            while(true) {
                connectSoftware();
                connectSojung();
                Thread.sleep(6 * HOURS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*

      소프트웨어 중심대학 사업단 공지사항 파싱

     */

    private void connectSojung() throws IOException, ClassNotFoundException {
        List<String> tables = getReader(sojung.concat("?bd=notice"), "<tr>", "</tr>");

        List<HrefInfo> hrefInfos = new ArrayList<>();
        Matcher matcher;
        for(String str : tables) {
            matcher = sjDatePattern.matcher(str);
            if(!matcher.find()) continue;
            String date = matcher.group();

            matcher = sjHrefPattern.matcher(str);
            matcher.find();
            String link = sojung.concat("view/").concat(matcher.group());

            matcher = sjStrongPattern.matcher(str);
            matcher.find();
            String name = matcher.group();

            hrefInfos.add(new HrefInfo(name, date, link));
        }

        List<HrefInfo> needPrint = new RequestSerializer().update(RequestSerializer.RequestSiteType.SOJUNG, hrefInfos);
        sendHrefInfo(needPrint, softwareTC, new java.awt.Color(0xDF1D8579, true), "소프트웨어중심대학사업단 공지사항");
    }


    /*

        소프트웨어공학과 공지사항 파싱

     */
    private void connectSoftware() throws IOException, ClassNotFoundException {
        List<String> tables = getReader(software.concat("/bbs/sw/1038/artclList.do"), "<tr class=\"notice", "</tr>");

        List<HrefInfo> hrefs = new ArrayList<>();
        Matcher matcher;
        for(String tb : tables) {
            matcher = swHrefPattern.matcher(tb);
            matcher.find();
            String url = matcher.group();

            matcher = swStrongPattern.matcher(tb);
            matcher.find();
            String title = matcher.group();

            matcher = swDatePattern.matcher(tb);
            matcher.find();
            String date = matcher.group();

            hrefs.add(new HrefInfo(title.replace("&quot;", "\""), date, software.concat(url.replace("<a href=\"", ""))));
        }

        List<HrefInfo> needPrint = new RequestSerializer().update(RequestSerializer.RequestSiteType.SOFTWARE, hrefs);
        sendHrefInfo(needPrint, softwareTC, new java.awt.Color(0x7ACBCB), "소프트웨어공학과 공지사항");
    }
    
    /*
    
        유틸리티
    
     */

    private List<String> getReader(String url, String startKey, String endKey) throws IOException {
        EnvironmentData.logger.sendHTTPRequest(url);
        URL target = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestMethod("GET");

        if(conn.getResponseCode() != 200) {
            EnvironmentData.logger.responseHTTPRequest(url, conn.getResponseCode());
            System.out.println(software+" 와의 연결에 실패했습니다. ResponseCode : " + conn.getResponseCode());
            return null;
        }
        EnvironmentData.logger.responseHTTPRequest(url, conn.getResponseCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        boolean accept = false;
        StringBuilder builder = new StringBuilder();
        List<String> tables = new ArrayList<>();
        String line;

        while((line = br.readLine()) != null) {
            char[] chars = line.toCharArray();
            if(chars.length == 0) continue;
            int start;
            for(start = 0 ; start < chars.length ; start++) {
                if(chars[start] != ' ' && chars[start] != '\t') break;
            }
            if(start == line.length()) continue;

            String realLine = line.substring(start);
            if(realLine.startsWith(startKey)) accept = true;
            else if(realLine.endsWith(endKey)) accept = false;

            if(accept) {
                builder.append(realLine.replace("\r", "").replace("\n", ""));
            } else {
                if(builder.length() > 0) {
                    tables.add(builder.toString());
                    builder = new StringBuilder();
                }
            }
        }
        br.close();
        conn.disconnect();

        return tables;
    }

    private void sendHrefInfo(List<HrefInfo> sendData, long tcId, java.awt.Color color, String footer) {
        TextChannel channel = EnvironmentData.getInstance().getMainGuild().getTextChannelById(tcId);
        Collections.sort(sendData);
        for(HrefInfo info : sendData) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(color);
            builder.setFooter(footer);
            builder.setTitle(info.getTitle(), info.getLink());
            builder.addField("작성일자", info.getDate(), true);
            builder.setDescription(info.getTitle());

            channel.sendMessageEmbeds(builder.build()).queue();
        }

    }

    private void postParameters(HttpURLConnection conn, String parameter) throws IOException {
        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        dos.writeBytes(parameter);
        dos.flush();
        dos.close();
    }
}

import com.google.gson.Gson;
import netscape.javascript.JSObject;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by root on 06.04.15.
 */
public class GenerateHTML {
    public static PrintWriter pw;
    public static String template = "";
    public static String[] conversations_classic;
    public static String[] conversations_dnd;
    public static ArrayList<String> dates_dnd = null;
    public static ArrayList<String> dates_classic = null;
    public static String json;

    public static TreeMap<String, Integer> classesTotalClassic = null;
    public static TreeMap<String, Integer> classesTotalDnD = null;

    public static TreeMap<String, Integer> clubsTotal = null;
    public static TreeMap<String, Integer> clubsBelongingTotal = null;

    public static void main(String[] args) throws IOException {
        classesTotalClassic = new TreeMap<>();
        classesTotalDnD = new TreeMap<>();
        clubsTotal = new TreeMap<>();
        clubsBelongingTotal = new TreeMap<>();

        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream("dancers/template.html")));
        String curBf;

        while ((curBf = bf.readLine()) != null) {
            template += curBf;
        }

        bf = new BufferedReader(new InputStreamReader(new FileInputStream("dancers/dnd.csv")));
        curBf = bf.readLine();
        String w = "";
        while (!(curBf = bf.readLine()).toLowerCase().contains("история всех конкурсов и баллов")) {
//            System.out.println(curBf);
            w += curBf.replace("\n", " ");
        }
        String datesString = curBf.toLowerCase();
        String[] datesTmp = datesString.split(";");
        dates_dnd = new ArrayList<>();
        for (String s : datesTmp) {
            if (s.matches("\\d{1,2}\\.\\d{1,2}\\.\\d+")) {
                dates_dnd.add(s);
            } else {
                System.out.println(s);
            }
        }
        dates_dnd.remove(0);
        w = w.substring(w.indexOf(";\"") + 2);
        conversations_dnd = w.split(";");
        for (String s : conversations_dnd) {
            s = s.replaceAll("\"", "");
        }
        while (!(curBf = bf.readLine()).toLowerCase().contains("код;пол;фамилия")) {
            if (curBf.toLowerCase().contains("история всех конкурсов и баллов")) {
            }
        }

        TreeMap<String, String> map = new TreeMap<>();
        while ((curBf = bf.readLine()) != null) {
            map.put(curBf.split(";")[0], curBf);
        }

        bf = new BufferedReader(new InputStreamReader(new FileInputStream("dancers/classic.csv")));
        curBf = bf.readLine();
        w = "";
        while (!(curBf = bf.readLine()).toLowerCase().contains("история всех конкурсов и баллов")) {
            w += curBf.replace("\n", " ");
        }
        datesString = curBf.toLowerCase();
        datesTmp = datesString.split(";");
        dates_classic = new ArrayList<>();
        for (String s : datesTmp) {
            if (s.matches("\\d{1,2}\\.\\d{1,2}\\.\\d+")) {
                dates_classic.add(s);
            } else {
                System.out.println(s);
            }
        }
        dates_classic.remove(0);
        w = w.substring(w.indexOf(";\"") + 2);
        conversations_classic = w.split(";");
        for (String s : conversations_classic) {
            s = s.replaceAll("\"", "");
        }
        while (!(curBf = bf.readLine()).toLowerCase().contains("код;пол;фамилия")) {
        }

        json = "var data = [";
        curBf = bf.readLine();
        while (!curBf.contains("ЗНАЧ") && !curBf.contains("EOF")) {
//            if (curBf.contains("5048") || curBf.contains("6472") || curBf.contains("6776")) {
//                System.out.println(curBf);
            generatePage(curBf, map.get(curBf.split(";")[0]));
//            }
            curBf = bf.readLine();
        }
        json += "]";
        json = replaceLast(json, ",", "");
        pw = new PrintWriter("pages/data.js");
        pw.print(json);
        pw.close();

        pw = new PrintWriter("pages/main_stat.js");
        Gson gson = new Gson();
        pw.println("var classesClassic = " + gson.toJson(classesTotalClassic) + ";");
        pw.println("var classesDnD = " + gson.toJson(classesTotalDnD) + ";");

        List<Map.Entry<String, Integer>> clubsTop10 = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : clubsTotal.entrySet()) {
            clubsTop10.add(entry);
        }
        Collections.sort(clubsTop10, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                }
                if (o1.getValue() < o2.getValue()) {
                    return 1;
                }
                return 0;
            }
        });
        clubsTop10 = clubsTop10.subList(0, 9);
        String clubsJson = "{";
        for (Map.Entry<String, Integer> entry : clubsTop10) {
            clubsJson += "\'" + entry.getKey() + "\': " + entry.getValue() + ",";
        }
        clubsJson += "}";
        clubsJson = replaceLast(clubsJson, ",", "");
        pw.println("var clubs = " + clubsJson + ";");

        List<Map.Entry<String, Integer>> clubsBelongingTop10 = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : clubsBelongingTotal.entrySet()) {
            clubsBelongingTop10.add(entry);
        }
        Collections.sort(clubsBelongingTop10, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                }
                if (o1.getValue() < o2.getValue()) {
                    return 1;
                }
                return 0;
            }
        });
        clubsBelongingTop10 = clubsBelongingTop10.subList(0, 10);
        String clubsBelongingJson = "{";
        for (Map.Entry<String, Integer> entry : clubsBelongingTop10) {
            clubsBelongingJson += "\'" + entry.getKey() + "\': " + entry.getValue() + ",";
        }
        clubsBelongingJson += "}";
        clubsBelongingJson = replaceLast(clubsBelongingJson, ",", "");
        pw.println("var clubsBelonging = " + clubsBelongingJson + ";");

        pw.close();
    }

    public static String replaceLast(String string, String substring, String replacement) {
        int index = string.lastIndexOf(substring);
        if (index == -1)
            return string;
        return string.substring(0, index) + replacement
                + string.substring(index + substring.length());
    }

    public static String strJoin(String[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }

    public static void generatePage(String classic, String dnd) throws FileNotFoundException {
//        System.out.println(classic);
//        System.out.println(dnd);
        String[] ch_classic = classic.split(";");
        String[] ch_dnd = dnd.split(";");

        json += "\t{\n" +
                "\t\tlabel : \"" + ch_classic[0] + "\",\n" +
                "\t\tvalue : \"" + ch_classic[0] + " " + ch_classic[2].replaceAll("\"", "") + "\"\n" +
                "\t},\n";
        json += "\t{\n" +
                "\t\tlabel : \"" + ch_classic[2].replaceAll("\"", "") + "\",\n" +
                "\t\tvalue : \"" + ch_classic[0] + " " + ch_classic[2].replaceAll("\"", "") + "\"\n" +
                "\t},\n";

        String fileName = ch_classic[0];
        String sdj = "{"; // Single Dancer Json
        PrintWriter printWriterSingleDancerJson = new PrintWriter("ajax/" + fileName + ".json");
//        pw = new PrintWriter("pages/" + fileName + ".html");

//        String html = "".concat(template);

//        html = html.replaceAll("%id%", ch_classic[0]);
        sdj += "\"id\":\"" + ch_classic[0] + "\",";

        String tmp = ch_classic[2].substring(0, ch_classic[2].indexOf("("));
//        html = html.replaceAll("%name%", tmp);
//        html = html.replaceAll("%link%", "pages/" + fileName + ".html");
        sdj += "\"name\":\"" + tmp + "\",";
        sdj += "\"link\":\"" + "pages/" + fileName + ".html" + "\",";

        tmp = ch_classic[2].substring(ch_classic[2].indexOf("(") + 1, ch_classic[2].length() - 1);
//        html = html.replace("%club%", tmp);
        sdj += "\"club\":\"" + tmp + "\",";

        String[] currentClubs = tmp.split(",");
        Arrays.sort(currentClubs);

        for (String club : currentClubs) {
            if (clubsTotal.containsKey(club)) {
                int opl = clubsTotal.get(club) + 1;
                clubsTotal.put(club, opl);
            } else {
                clubsTotal.put(club, 1);
            }
        }

        if (currentClubs.length > 1) {
            String w = strJoin(currentClubs, ", ");
            if (clubsBelongingTotal.containsKey(w)) {
                int opl = clubsBelongingTotal.get(w) + 1;
                clubsBelongingTotal.put(w, opl);
            } else {
                clubsBelongingTotal.put(w, 1);
            }
        }

//        html = html.replace("%class_classic%", ch_classic[4]);
//        html = html.replace("%class_dnd%", ch_dnd[4]);
        sdj += "\"class_classic\":\"" + ch_classic[4] + "\",";
        sdj += "\"class_dnd\":\"" + ch_dnd[4] + "\",";

        if (classesTotalClassic.containsKey(ch_classic[4])) {
            int opl = classesTotalClassic.get(ch_classic[4]) + 1;
            classesTotalClassic.put(ch_classic[4], opl);
        } else {
            classesTotalClassic.put(ch_classic[4], 1);
        }

        if (classesTotalDnD.containsKey(ch_dnd[4])) {
            int opl = classesTotalDnD.get(ch_dnd[4]) + 1;
            classesTotalDnD.put(ch_dnd[4], opl);
        } else {
            classesTotalDnD.put(ch_dnd[4], 1);
        }

//        html = html.replace("%A%", ch_classic[9]);
//        html = html.replace("%B%", ch_classic[8]);
//        html = html.replace("%C%", ch_classic[7]);
//        html = html.replace("%D%", ch_classic[6]);
//        html = html.replace("%E%", ch_classic[5]);
//
//        html = html.replace("%Ch%", ch_dnd[9]);
//        html = html.replace("%S%", ch_dnd[8]);
//        html = html.replace("%M%", ch_dnd[7]);
//        html = html.replace("%RS%", ch_dnd[6]);
//        html = html.replace("%Beg%", ch_dnd[5]);

        sdj += "\"A\":\"" + ch_classic[9] + "\",";
        sdj += "\"B\":\"" + ch_classic[8] + "\",";
        sdj += "\"C\":\"" + ch_classic[7] + "\",";
        sdj += "\"D\":\"" + ch_classic[6] + "\",";
        sdj += "\"E\":\"" + ch_classic[5] + "\",";

        sdj += "\"Ch\":\"" + ch_dnd[9] + "\",";
        sdj += "\"S\":\"" + ch_dnd[8] + "\",";
        sdj += "\"M\":\"" + ch_dnd[7] + "\",";
        sdj += "\"RS\":\"" + ch_dnd[6] + "\",";
        sdj += "\"Beg\":\"" + ch_dnd[5] + "\",";


        String story_classic = "";
        for (int i = 12; i < ch_classic.length; i++) {
            if (!ch_classic[i].equals("")) {
                story_classic += dates_classic.get(i - 12) + " — " + conversations_classic[i - 12].replaceAll("\"", "") + " : " + ch_classic[i] + "<br><br>";
            }
        }

        String story_dnd = "";
        for (int i = 12; i < ch_dnd.length; i++) {
            if (!ch_dnd[i].equals("")) {
                story_dnd += dates_dnd.get(i - 12) + " — " + conversations_dnd[i - 12].replaceAll("\"", "") + " : " + ch_dnd[i] + "<br><br>";
            }
        }

//        html = html.replace("%story_classic%", story_classic);
//        html = html.replace("%story_dnd%", story_dnd);

        sdj += "\"story_classic\":\"" + story_classic + "\",";
        sdj += "\"story_dnd\":\"" + story_dnd + "\"}";
//        System.out.println(story_classic.replaceAll("<br>", "\n"));
//        System.out.println(story_dnd.replaceAll("<br>", "\n"));
        printWriterSingleDancerJson.print(sdj);
        printWriterSingleDancerJson.close();

//        pw.print(html);
//        pw.close();
    }
}
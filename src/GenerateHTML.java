import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

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

    public static void main(String[] args) throws IOException {

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
    }

    public static String replaceLast(String string, String substring, String replacement) {
        int index = string.lastIndexOf(substring);
        if (index == -1)
            return string;
        return string.substring(0, index) + replacement
                + string.substring(index + substring.length());
    }

    public static void generatePage(String classic, String dnd) throws FileNotFoundException {
//        System.out.println(classic);
//        System.out.println(dnd);
        String[] ch_classic = classic.split(";");
        String[] ch_dnd = dnd.split(";");

        json += "{" +
                "label : \"" + ch_classic[0] + "\"," +
                "value : \"" + ch_classic[0] + " " + ch_classic[2] + "\"" +
                "},";
        json += "{" +
                "label : \"" + ch_classic[2] + "\"," +
                "value : \"" + ch_classic[0] + " " + ch_classic[2] + "\"" +
                "},";

        String fileName = ch_classic[0];
        pw = new PrintWriter("pages/" + fileName + ".html");

        String html = "".concat(template);

        html = html.replaceAll("%id%", ch_classic[0]);

        String tmp = ch_classic[2].substring(0, ch_classic[2].indexOf("("));
        html = html.replaceAll("%name%", tmp);
        html = html.replaceAll("%link", "pages/" + fileName + ".html");

        tmp = ch_classic[2].substring(ch_classic[2].indexOf("(") + 1, ch_classic[2].length() - 1);
        html = html.replace("%club%", tmp);

        html = html.replace("%class_classic%", ch_classic[4]);
        html = html.replace("%class_dnd%", ch_dnd[4]);

        html = html.replace("%A%", ch_classic[9]);
        html = html.replace("%B%", ch_classic[8]);
        html = html.replace("%C%", ch_classic[7]);
        html = html.replace("%D%", ch_classic[6]);
        html = html.replace("%E%", ch_classic[5]);

        html = html.replace("%Ch%", ch_dnd[9]);
        html = html.replace("%S%", ch_dnd[8]);
        html = html.replace("%M%", ch_dnd[7]);
        html = html.replace("%RS%", ch_dnd[6]);
        html = html.replace("%Beg%", ch_dnd[5]);


        String story_classic = "";
        for (int i = 12; i < ch_classic.length; i++) {
            if (!ch_classic[i].equals("")) {
                story_classic += dates_classic.get(i - 12) + " " + conversations_classic[i - 12].replaceAll("\"", "") + " : " + ch_classic[i] + "<br><br>";
            }
        }

        String story_dnd = "";
        for (int i = 12; i < ch_dnd.length; i++) {
            if (!ch_dnd[i].equals("")) {
                story_dnd += dates_dnd.get(i - 12) + " " + conversations_dnd[i - 12].replaceAll("\"", "") + " : " + ch_dnd[i] + "<br><br>";
            }
        }

        html = html.replace("%story_classic%", story_classic);
        html = html.replace("%story_dnd%", story_dnd);
//        System.out.println(story_classic.replaceAll("<br>", "\n"));
//        System.out.println(story_dnd.replaceAll("<br>", "\n"));
        pw.print(html);
        pw.close();
    }
}

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

enum Matchcolor {
    B(0), Y(1), G(2);
    private final int value;

    Matchcolor(int value) {
        this.value = value;
    }
}

public class exhaustiveSearch {
    static int MAXATTEMPT = 6;
    static int WORDSIZE = 5;
    static int[] INDEXTABLE = {1, 3, 9, 27, 81};
    static char USEDCHAR = '0';
    static String ANSFILEPATH = ".\\src\\words.txt";
    static String POOLFILEPATH = ".\\src\\all.txt";
    static Set<String> WORDPOOL = new HashSet<String>();
    static int PATTERNS = 243;
    static double[] ALPHABETFREQ = {
            0.084966, 0.020720, 0.045388, 0.033844, 0.111607,
            0.018121, 0.024705, 0.030034, 0.075448, 0.001965,
            0.011016, 0.054893, 0.030129, 0.066544, 0.071635,
            0.031671, 0.001962, 0.075809, 0.057351, 0.069509,
            0.036308, 0.010074, 0.012899, 0.002902, 0.017779,
            0.002721
    };      // by Concise Oxford Dictionary (9th edition, 1995)


    public static void main(String[] args) throws IOException {
        int count = 0;       // 回答回数
        String[] inputWords = new String[6];  // 回答履歴
        int[] result = new int[6];    // 回答のパターン
        ArrayList<String> dic = new ArrayList<String>();    // ありうる回答
        dic = loadWords();      // 答えの単語を読み込む
        WORDPOOL = genpool();
        System.out.println("MAXIMUM ENTROPY WORD: " + calcMaximumentropyword(dic));       // 最初

        BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in));

        for (int i = 0; i < MAXATTEMPT; i++) {
            System.out.println("attempt count:" + (i + 1));
            String s;

            while (true) {        // 正しい入力がなされるまで続ける
                System.out.print("words?: ");
                s = bfr.readLine().toLowerCase();
                if (s.length() == WORDSIZE && WORDPOOL.contains(s)) {
                    inputWords[i] = s;        // 入力履歴に入れる
                    count++;
                    break;
                } else {
                    System.out.println("illegal length or word");
                    continue;
                }
            }

            Tfthree tftt = makeTft(dic, s);
            //tftt.output();
            int tmpindex;
            while (true) {        // 正しい入力がなされるまで続ける
                System.out.print("return?: ");
                s = bfr.readLine().toLowerCase();
                if (s.length() == WORDSIZE) {
                    tmpindex = returnToindex(s);
                    break;
                } else {
                    System.out.println("illegal length");
                    continue;
                }
            }
            dic = tftt.get(tmpindex);

            System.out.println("MAXIMUM ENTROPY WORD: " + calcMaximumentropyword(dic));


            outputDictionary(dic);
            result[count - 1] = dic.size();
            System.out.println();
            System.gc();
            if (dic.size() == 1) {
                System.out.println(dic.get(0));
                System.out.println();
                stat(result, inputWords, count);

                System.exit(0);
            }
        }

        stat(result, inputWords, count);


    }

    static String calcMaximumentropyword(ArrayList<String> dic) {        // 単語のエントロピーを計算
        double maxentropy = -5000d;
        int maxentropyindex = 0;
        for (int h = 0; h < dic.size(); h++) {
            double tmp = 0;
            String tmpword = delmult(dic.get(h));
            for (int j = 0; j < tmpword.length(); j++) {
                tmp += -Math.log(ALPHABETFREQ[tmpword.charAt(j) - 'a']) * ALPHABETFREQ[tmpword.charAt(j) - 'a'];
            }
            if (maxentropy < tmp) {
                maxentropy = tmp;
                maxentropyindex = h;
            }
        }
        return dic.get(maxentropyindex);
    }

    static void stat(int[] res, String[] words, int count) {
        System.out.println("Statistics");
        for (int i = 0; i < count; i++) {
            System.out.println("Attempt " + (i + 1) + ": word:" + words[i] + " candidate(s):" + res[i]);
        }

    }

    static String delmult(String s) {        // 文字の重複を消去 エントロピー計算に使う
        Set<Character> rr = new HashSet<Character>();
        for (int i = 0; i < s.length(); i++) {
            rr.add(s.charAt(i));
        }
        String ret = "";
        for (char sss : rr) {
            ret = ret + sss;
        }
        return ret;
    }


    static void outputDictionary(ArrayList<String> di) {     // 候補辞書を出力
        System.out.println(di.toString());
        System.out.println("size of the dict: " + di.size());
    }

    static int returnToindex(String s) {     // GBGGYみたいな返答から辞書のインデックスを返す
        int ret = 0;
        for (int i = 0; i < WORDSIZE; i++) {
            if (s.charAt(i) == 'g') {
                ret += Matchcolor.G.ordinal() * INDEXTABLE[i];
            } else if (s.charAt(i) == 'y') {
                ret += Matchcolor.Y.ordinal() * INDEXTABLE[i];
            } else if (s.charAt(i) == 'b') {
                ret += Matchcolor.B.ordinal() * INDEXTABLE[i];
            }
        }
        return ret;
    }

    static Tfthree makeTft(ArrayList<String> dic, String s) {        // 入力sと候補のワードリストから243分木を作る
        Tfthree ret = new Tfthree();
        for (int i = 0; i < dic.size(); i++) {
            int index = matchToindex(s, dic.get(i));
            ret.add(dic.get(i), index);      // 答えがsであると仮定した時のワードセットに含まれる単語がどういう答え(緑緑緑灰灰とかのレスポンス)になるか
        }
        return ret;
    }

    static public int matchToindex(String a, String b) {     // 入力aが答えだと仮定した時のワードセットにある単語bの値
        int[] index = new int[WORDSIZE];
        Arrays.fill(index, Matchcolor.B.ordinal());
        String tmpb = b;        // 文字を変える時
        String tmpa = a;
        for (int i = 0; i < WORDSIZE; i++) {     //　完全一致
            if (tmpa.charAt(i) == tmpb.charAt(i)) {
                if (tmpa.charAt(i) != USEDCHAR && tmpb.charAt(i) != USEDCHAR) {
                    index[i] = Matchcolor.G.ordinal();
                    tmpb = rem(tmpb, i);
                    tmpa = rem(tmpa, i);
                }
            }
        }
        for (int i = 0; i < WORDSIZE; i++) {      //　部分一致
            for (int j = 0; j < WORDSIZE; j++) {
                if (i != j) {
                    if (tmpa.charAt(i) == tmpb.charAt(j)) {
                        if (tmpa.charAt(i) != USEDCHAR && tmpb.charAt(j) != USEDCHAR) {
                            index[i] = Matchcolor.Y.ordinal();
                            tmpa = rem(tmpa, i);
                            tmpb = rem(tmpb, j);
                        }
                    }
                }
            }
        }
        int ret = 0;
        for (int i = 0; i < WORDSIZE; i++) {
            ret += index[i] * INDEXTABLE[i];
        }
        return ret;
    }

    public static String rem(String s, int index) {   // 同じ文字が2回以上ある時の比較時の文字消し
        String ret = "";
        for (int i = 0; i < WORDSIZE; i++) {
            if (i != index) {
                ret = ret + s.charAt(i);
            } else {
                ret = ret + USEDCHAR;
            }
        }
        return ret;
    }

    public static ArrayList<String> loadWords() throws IOException {        // 最初の辞書の読み込み
        /* using for the Debugging purpose
        Path p = Paths.get("").toAbsolutePath();
        System.out.println(p.toString());
        */
        File f = new File(ANSFILEPATH);
        ArrayList<String> ret = new ArrayList<String>();
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String t = "";
        while ((t = br.readLine()) != null) {
            ret.add(t);
        }
        return ret;
    }

    public static Set<String> genpool() throws IOException {    // 候補のワードプールをつくって入力単語の範囲を作る
        File f = new File(POOLFILEPATH);
        Set<String> ret = new HashSet<String>();
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String t = "";
        while ((t = br.readLine()) != null) {
            ret.add(t);
        }
        return ret;
    }

    static class Tfthree {  // two four three tree      243分木
        ArrayList<ArrayList<String>> tft = new ArrayList<ArrayList<String>>();

        Tfthree() {
            tft = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < PATTERNS; i++) {
                tft.add(new ArrayList<String>());
            }
        }

        void add(String s, int index) {
            tft.get(index).add(s);
        }

        ArrayList<String> get(int index) {
            return tft.get(index);
        }

        void output() {
            for (int i = 0; i < PATTERNS; i++) {
                System.out.println("Attribute " + (i + 1) + ": Size: " + tft.get(i).size() + " " + tft.get(i).toString());
            }
        }
    }

}
package jp.ac.meijou.android.cyc;

import java.util.Map;

public class Gist {
    public Map<String, GistFile> files;
    public static class GistFile {
        public String content;
    }
}

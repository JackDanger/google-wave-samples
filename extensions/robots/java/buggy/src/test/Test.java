
package test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test<T extends Test> {

  String name = "austin";

  private static class Data extends Test{
    String doWork() {
      return "done!";
    }
  }

  private String doWork() {
    return "here";
  }

  public Test() {
  }

  public void bar(T t) {
    t.doWork();
  }

  public <X>void foo(X x) {
    X copy = (X) new Object();
  }

  private static String getTrixEmbedUrl(String trixSheetUrl) {
    String trixEmbedUrl = null;
    try {
      URL url = new URL(trixSheetUrl);
      String protocol = url.getProtocol();
      String host = url.getHost();
      String port = (url.getPort() > 0) ? ":" + url.getPort() : "";
      String path = url.getPath();
      trixEmbedUrl = protocol + "://" + host + port;
      if (path.startsWith("/a/")) {
        // This is a dasher domain.
        String re = "^/a/[^/]+";
        //String re = "\\/a\\/[a-zA-Z0-9_\\-]+\\.[a-zA-Z]+";
        Pattern p = Pattern.compile(re, Pattern.DOTALL);
        Matcher m = p.matcher(path);
        if (m.find()) {
          trixEmbedUrl += m.group(0);
        }
      }
    } catch (MalformedURLException e) {
    }
    return trixEmbedUrl;
  }

  public static void main(String[] s) {
    Test<Data> test = new Test<Data>();
    test.bar(new Data());

    String url = "https://spreadsheets.google.com/a/google.com/ccc?key=tEFWW49SzAiJKnLWuw42rTQ";
    long num = 394834l;
    System.out.println(String.format("%d", num));

  }
}

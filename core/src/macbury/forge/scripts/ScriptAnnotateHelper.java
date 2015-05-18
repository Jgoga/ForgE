package macbury.forge.scripts;

import com.badlogic.gdx.utils.Array;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by macbury on 12.05.15.
 */
public class ScriptAnnotateHelper {
  private static Pattern FN_ARGS        = Pattern.compile("^function\\s*[^\\(]*\\(\\s*([^\\)]*)\\)");
  private static Pattern FN_ARG_SPLIT   = Pattern.compile(",");
  private static Pattern FN_ARG         = Pattern.compile("^\\s*(_?)(.+?)\\1\\s*$");
  private static Pattern STRIP_COMMENTS = Pattern.compile("((\\/\\/.*$)|(\\/\\*[\\s\\S]*?\\*\\/)|(\\s))");


  public static Array<String> annotate(BaseFunction function) {
    Array<String> inject   = new Array<String>();
    String fnText          = STRIP_COMMENTS.matcher(Context.toString(function)).replaceAll("");
    Matcher argDeclMatcher = FN_ARGS.matcher(fnText);
    if (argDeclMatcher.find()) {
      String args[] = FN_ARG_SPLIT.split(argDeclMatcher.group(1));
      for (String arg : args) {
        Matcher argMatcher = FN_ARG.matcher(arg);
        if (argMatcher.find()) {
          inject.add(argMatcher.group(0));
        }
      }
    }
    return inject;
  }
}
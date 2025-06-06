package kmark.lexer;

import kmark.MarkdownTokenTypes;
import kmark.IElementType;
import kmark.lexer.GeneratedLexer;

/* Auto generated File */
%%

%class _MarkdownLexer
%unicode
%public

%function advance
%type IElementType

%implements GeneratedLexer

%{
  private static class Token extends MarkdownTokenTypes {}

  private List<Integer> stateStack = new ArrayList<Integer>();

  private boolean isHeader = false;

  private ParseDelimited parseDelimited = new ParseDelimited();

  private static class ParseDelimited {
    char exitChar = 0;
    IElementType returnType = null;
    boolean inlinesAllowed = true;
  }

  private static class LinkDef {
    boolean wasUrl;
    boolean wasParen;
  }

  private static class HtmlHelper {
    private static final String BLOCK_TAGS_STRING =
            "article, header, aside, hgroup, blockquote, hr, iframe, body, li, map, button, " +
            "object, canvas, ol, caption, output, col, p, colgroup, pre, dd, progress, div, " +
            "section, dl, table, td, dt, tbody, embed, textarea, fieldset, tfoot, figcaption, " +
            "th, figure, thead, footer, footer, tr, form, ul, h1, h2, h3, h4, h5, h6, video, " +
            "script, style";

    static final Set<String> BLOCK_TAGS = getBlockTagsSet();

    private static Set<String> getBlockTagsSet() {
      Set<String> result = new HashSet<String>();
      String[] tags = BLOCK_TAGS_STRING.split(", ");
      for (String tag : tags) {
        result.add(tag);
      }
      return result;
    }
  }

  private static IElementType getDelimiterTokenType(char c) {
    switch (c) {
      case '"': return MarkdownTokenTypes.DOUBLE_QUOTE;
      case '\'': return MarkdownTokenTypes.SINGLE_QUOTE;
      case '(': return MarkdownTokenTypes.LPAREN;
      case ')': return MarkdownTokenTypes.RPAREN;
      case '[': return MarkdownTokenTypes.LBRACKET;
      case ']': return MarkdownTokenTypes.RBRACKET;
      case '<': return MarkdownTokenTypes.LT;
      case '>': return MarkdownTokenTypes.GT;
      default: return MarkdownTokenTypes.BAD_CHARACTER;
    }
  }

  private IElementType parseDelimited(IElementType contentsType, boolean allowInlines) {
    char first = yycharat(0);
    char last = yycharat(yylength() - 1);

    stateStack.push(yystate());

    parseDelimited.exitChar = last;
    parseDelimited.returnType = contentsType;
//    parseDelimited.inlinesAllowed = allowInlines;
    parseDelimited.inlinesAllowed = true;

    yybegin(PARSE_DELIMITED);

    yypushback(yylength() - 1);
    return getDelimiterTokenType(first);
  }

  private void processEol() {
    int newlinePos = 1;
    while (newlinePos < yylength() && yycharat(newlinePos) != '\n') {
      newlinePos++;
    }

    // there is always one at 0 so that means there are two at least
    if (newlinePos != yylength()) {
      yypushback(yylength() - newlinePos);
      return;
    }

    yybegin(YYINITIAL);
    yypushback(yylength() - 1);

    isHeader = false;
  }

  private void popState() {
    if (stateStack.isEmpty()) {
      yybegin(AFTER_LINE_START);
    }
    else {
      yybegin(stateStack.pop());
    }
  }

  private void resetState() {
    yypushback(yylength());

    popState();
  }

  private String getTagName() {
    if (yylength() > 1 && yycharat(1) == '/') {
      return yytext().toString().substring(2, yylength() - 1).trim();
    }
    return yytext().toString().substring(1);
  }

  private boolean isBlockTag(String tagName) {
    return HtmlHelper.BLOCK_TAGS.contains(tagName.toLowerCase());
  }

  private boolean canInline() {
    return yystate() == AFTER_LINE_START || yystate() == PARSE_DELIMITED && parseDelimited.inlinesAllowed;
  }

  private IElementType getReturnGeneralized(IElementType defaultType) {
    if (canInline()) {
      return defaultType;
    }
    return parseDelimited.returnType;
  }

  private int countChars(CharSequence s, char c) {
    int result = 0;
    for (int i = 0; i < s.length(); ++i) {
      if (s.charAt(i) == c)
        result++;
    }
    return result;
  }

%}

ALPHANUM = [\p{Letter}\p{Number}]
WHITE_SPACE = [ \t\f]
EOL = \R
ANY_CHAR = [^]

DOUBLE_QUOTED_TEXT = \" (\\\" | [^\n\"])* \"
SINGLE_QUOTED_TEXT = "'" (\\"'" | [^\n'])* "'"
QUOTED_TEXT = {SINGLE_QUOTED_TEXT} | {DOUBLE_QUOTED_TEXT}

HTML_COMMENT = "<!" "-"{2,4} ">" | "<!--" ([^-] | "-"[^-])* "-->"
PROCESSING_INSTRUCTION = "<?" ([^?] | "?"[^>])* "?>"
DECLARATION = "<!" [A-Z]+ {WHITE_SPACE}+ [^>] ">"
CDATA = "<![CDATA[" ([^\]] | "]"[^\]] | "]]"[^>])* "]]>"

TAG_NAME = [a-zA-Z]{ALPHANUM}*
ATTRIBUTE_NAME = [a-zA-Z:-] ({ALPHANUM} | [_.:-])*
ATTRIBUTE_VALUE = {QUOTED_TEXT} | [^ \t\f\n\r\"\'=<>`]+
ATTRIBUTE_VALUE_SPEC = {WHITE_SPACE}* "=" {WHITE_SPACE}* {ATTRIBUTE_VALUE}
ATTRUBUTE = {WHITE_SPACE}+ {ATTRIBUTE_NAME} {ATTRIBUTE_VALUE_SPEC}?
OPEN_TAG = "<" {TAG_NAME} {ATTRUBUTE}* {WHITE_SPACE}* "/"? ">"
CLOSING_TAG = "</" {TAG_NAME} {WHITE_SPACE}* ">"
HTML_TAG = {OPEN_TAG} | {CLOSING_TAG} | {HTML_COMMENT} | {PROCESSING_INSTRUCTION} | {DECLARATION} | {CDATA}

TAG_START = "<" {TAG_NAME}
TAG_END = "</" {TAG_NAME} {WHITE_SPACE}* ">"

SCHEME = [a-zA-Z]+
AUTOLINK = "<" {SCHEME} ":" [^ \t\f\n<>]+ ">"
EMAIL_AUTOLINK = "<" [a-zA-Z0-9.!#$%&'*+/=?\^_`{|}~-]+ "@"[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])? (\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)* ">"

%state TAG_START, AFTER_LINE_START, PARSE_DELIMITED, CODE

%%



<YYINITIAL> {

  // blockquote
  {WHITE_SPACE}{0,3} ">" {
    return MarkdownTokenTypes.BLOCK_QUOTE;
  }

  {ANY_CHAR} {
    resetState();
  }

  {WHITE_SPACE}* {EOL} {
    resetState();
  }
}


<AFTER_LINE_START, PARSE_DELIMITED> {
  // The special case of a backtick
  \\"`"+ {
    return getReturnGeneralized(MarkdownTokenTypes.ESCAPED_BACKTICKS);
  }

  // Escaping
  \\[\\\"'`*_{}\[\]()#+.,!:@#$%&~<>/-] {
    return getReturnGeneralized(MarkdownTokenTypes.TEXT);
  }

  // Backticks (code span)
  "`"+ {
    if (canInline()) {
      return MarkdownTokenTypes.BACKTICK;
    }
    return parseDelimited.returnType;
  }

  // Emphasis
  {WHITE_SPACE}+ ("*" | "_") {WHITE_SPACE}+ {
    return getReturnGeneralized(MarkdownTokenTypes.TEXT);
  }

  "*" | "_" {
    return getReturnGeneralized(MarkdownTokenTypes.EMPH);
  }

  {AUTOLINK} { return parseDelimited(MarkdownTokenTypes.AUTOLINK, false); }
  {EMAIL_AUTOLINK} { return parseDelimited(MarkdownTokenTypes.EMAIL_AUTOLINK, false); }

  {HTML_TAG} { return MarkdownTokenTypes.HTML_TAG; }

}

<AFTER_LINE_START> {

  {WHITE_SPACE}+ {
    return MarkdownTokenTypes.WHITE_SPACE;
  }

  \" | "'"| "(" | ")" | "[" | "]" | "<" | ">" {
    return getDelimiterTokenType(yycharat(0));
  }
  ":" { return MarkdownTokenTypes.COLON; }
  "!" { return MarkdownTokenTypes.EXCLAMATION_MARK; }


  \\ / {EOL} {
    return MarkdownTokenTypes.HARD_LINE_BREAK;
  }

  {WHITE_SPACE}* ({EOL} {WHITE_SPACE}*)+ {
    int lastSpaces = yytext().toString().indexOf("\n");
    if (lastSpaces >= 2) {
      yypushback(yylength() - lastSpaces);
      return MarkdownTokenTypes.HARD_LINE_BREAK;
    }
    else if (lastSpaces > 0) {
      yypushback(yylength() - lastSpaces);
      return MarkdownTokenTypes.WHITE_SPACE;
    }

    processEol();
    return MarkdownTokenTypes.EOL;
  }

  // optimize and eat underscores inside words
  {ALPHANUM}+ (({WHITE_SPACE}+ | "_"+) {ALPHANUM}+)* {
    return MarkdownTokenTypes.TEXT;
  }

  {ANY_CHAR} { return MarkdownTokenTypes.TEXT; }

}

<PARSE_DELIMITED> {
  {EOL} { resetState(); }

  {EOL} | . {
    if (yycharat(0) == parseDelimited.exitChar) {
      yybegin(stateStack.pop());
      return getDelimiterTokenType(yycharat(0));
    }
    return parseDelimited.returnType;
  }

}

{ANY_CHAR} { return MarkdownTokenTypes.TEXT; }
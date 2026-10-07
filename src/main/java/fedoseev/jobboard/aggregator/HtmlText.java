package fedoseev.jobboard.aggregator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

/** Превращает HTML-описание вакансии в обычный текст с переносами строк. */
public final class HtmlText {

    private static final String BREAK = "%%BR%%";

    private HtmlText() {
    }

    public static String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document doc = Jsoup.parse(html);
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        doc.select("br").before(BREAK);
        doc.select("p, div, h1, h2, h3, h4, h5, h6, ul, ol").before(BREAK + BREAK);
        doc.select("li").before(BREAK + "• ");
        String text = Jsoup.clean(doc.body().html(), "", Safelist.none(),
                new Document.OutputSettings().prettyPrint(false));
        text = Parser.unescapeEntities(text, false)
                .replace(' ', ' ')
                .replaceAll("[ \\t]+", " ")
                .replace(BREAK, "\n")
                .replaceAll(" *\n *", "\n")
                .replaceAll("\n{3,}", "\n\n");
        return text.strip();
    }
}

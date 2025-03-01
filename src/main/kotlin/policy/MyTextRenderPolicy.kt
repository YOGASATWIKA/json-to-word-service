package id.pande.policy

import com.deepoove.poi.converter.ObjectToTextRenderDataConverter
import com.deepoove.poi.converter.ToRenderDataConverter
import com.deepoove.poi.data.BookmarkTextRenderData
import com.deepoove.poi.data.HyperlinkTextRenderData
import com.deepoove.poi.data.TextRenderData
import com.deepoove.poi.policy.AbstractRenderPolicy
import com.deepoove.poi.render.RenderContext
import com.deepoove.poi.util.StyleUtils
import com.deepoove.poi.util.TableTools
import com.deepoove.poi.xwpf.XWPFParagraphWrapper
import org.apache.poi.xwpf.usermodel.BreakType
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.util.regex.Pattern


class MyTextRenderPolicy : AbstractRenderPolicy<TextRenderData>() {
    companion object {
        private val converter: ToRenderDataConverter<Any, TextRenderData> = ObjectToTextRenderDataConverter()
    }

    @Throws(Exception::class)
    override fun cast(source: Any): TextRenderData {
        return converter.convert(source) as TextRenderData
    }

    override fun validate(data: TextRenderData?): Boolean {
        return data != null
    }

    @Throws(Exception::class)
    override fun doRender(context: RenderContext<TextRenderData>) {
        Helper.renderTextRun(context.run, context.data as TextRenderData)
    }

    object Helper {
        private const val REGEX_LINE_CHARACTER = "\\n|(\\r\\n)"
        private const val REGEX_MARKDOWN_BOLD = "(\\*\\*)(.*?)(\\*\\*)"
        private const val REGEX_MARKDOWN_HEAD = """^(#{1,3})\s+(.+)$"""
        private const val REGEX_MARKDOWN_POINT = """^\*\s+"""
        private const val REGEX_MARKDOWN_BEAUTIFY_LIST = """(\d+\.\s+)(.+)"""

        @JvmStatic
        fun renderTextRun(run: XWPFRun, data: TextRenderData) {
            var textRun = run

            if (data is HyperlinkTextRenderData) {
                textRun = createHyperlink(run, data.url)
            }

            StyleUtils.styleRun(textRun, data.style)
            val text = data.text ?: ""
            val fragment = text.split(Regex(REGEX_LINE_CHARACTER))
            if (fragment.isNotEmpty()) {
                checkMarkdown(textRun, fragment, data)
            }

            if (data is BookmarkTextRenderData) {
                createBookmark(textRun, data.bookmark)
            }
        }

        private fun createHyperlink(run: XWPFRun, url: String): XWPFRun {
            val paragraph = XWPFParagraphWrapper(run.parent as XWPFParagraph)
            val hyperlink = paragraph.insertNewHyperLinkRun(run, url)
            StyleUtils.styleRun(hyperlink, run)
            run.setText("", 0)
            return hyperlink
        }

        private fun createBookmark(textRun: XWPFRun, name: String) {
            val wrapper = XWPFParagraphWrapper(textRun.parent as XWPFParagraph)
            val bookmarkStart = wrapper.insertNewBookmark(textRun)
            bookmarkStart.name = name
        }

        private fun doDefault(parentRun: XWPFRun, lists: List<String>, data: TextRenderData) {
            parentRun.setText(lists[0], 0)
            val lineAtTable = lists.size > 1 && data !is HyperlinkTextRenderData && TableTools.isInsideTable(parentRun)

            for (i in 1 until lists.size) {
                if (lineAtTable) {
                    parentRun.addBreak(BreakType.TEXT_WRAPPING)
                } else {
                    parentRun.addCarriageReturn()
                }
                parentRun.setText(lists[i])
            }
        }

        private fun checkMarkdown(parentRun: XWPFRun, lists: List<String>, data: TextRenderData) {
            val paragraph = parentRun.parent as XWPFParagraph
            val parentStyle = StyleUtils.retriveStyle(parentRun)
            val boldPattern = Pattern.compile(REGEX_MARKDOWN_BOLD)
            val headPattern = Regex(REGEX_MARKDOWN_HEAD)
            val pointPattern = Regex(REGEX_MARKDOWN_POINT)
            val cleanListPattern = Regex(REGEX_MARKDOWN_BEAUTIFY_LIST)

            parentRun.setText("", 0)
            val lineAtTable = lists.size > 1 && data !is HyperlinkTextRenderData && TableTools.isInsideTable(parentRun)
            var lastRun: XWPFRun = paragraph.createRun()

            for (i in lists.indices) {
                val headResult = headPattern.matchEntire(lists[i])
                if (headResult != null) {
                    val (hashes, headerText) = headResult.destructured
                    val head = paragraph.createRun()
                    head.setText(headerText)
                    head.isBold = true
                    head.addCarriageReturn()
                    StyleUtils.styleRun(head, parentRun)
                    continue
                }

                var text = lists[i]
                text = text.replace(pointPattern,"- ")

                text = cleanListPattern.replace(text) { matchResult ->
                    val bullet = matchResult.groupValues[1].trim()
                    val content = matchResult.groupValues[2].trim()
                    "$bullet $content"
                }

                val boldMatcher = boldPattern.matcher(text)
                var lastPosition = 0

                while (boldMatcher.find()) {
                    val beforeBold = text.substring(lastPosition, boldMatcher.start())
                    if (beforeBold.isNotEmpty()) {
                        val beforeBoldRun = paragraph.createRun()
                        beforeBoldRun.setText(beforeBold,0)
                        StyleUtils.styleRun(beforeBoldRun, parentStyle)
                    }

                    val boldRun = paragraph.createRun()
                    boldRun.setText(boldMatcher.group(2),0)
                    boldRun.isBold = true
                    StyleUtils.styleRun(boldRun, parentStyle)

                    lastPosition = boldMatcher.end()
                    lastRun = boldRun
                }

                if (lastPosition < text.length) {
                    val afterBoldRun = paragraph.createRun()
                    afterBoldRun.setText(text.substring(lastPosition), 0)
                    lastRun = afterBoldRun
                    StyleUtils.styleRun(afterBoldRun, parentStyle)
                }

                if (lineAtTable) {
                    lastRun.addBreak(BreakType.TEXT_WRAPPING)
                } else {
                    lastRun.addCarriageReturn()
                }
            }

        }
    }
}

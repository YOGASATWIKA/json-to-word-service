package id.pande.compiler

import com.deepoove.poi.XWPFTemplate
import com.deepoove.poi.config.Configure
import com.deepoove.poi.data.DocxRenderData
import com.deepoove.poi.data.Includes
import id.pande.policy.MyTextRenderPolicy
import kotlinx.serialization.json.Json
import java.io.File

data class SKBParser(
    val title: String,
    val technical: DocxRenderData,
)

open class SKBCompiler(): EbookCompiler {

    override fun compile(source: String): String {
        val ebook = getDataFromJSON(source)

        val data = SKBParser(
            title = ebook.title,
            technical = Includes.ofLocal("./templates/skb/part.docx").setRenderModel(ebook.parts).create(),
        )

        val config = Configure.
            builder().
            bind("expanded", MyTextRenderPolicy()).
            bind("content",  MyTextRenderPolicy()).
            build()

        val pathOutput = "./output/skb/${ebook.title}.docx"

        try {
            XWPFTemplate.compile("./templates/skb/template.docx", config).render(data).writeToFile(pathOutput)

        }catch (e: Exception) {
            e.printStackTrace()
        }

        return pathOutput
    }

    private fun getDataFromJSON(jsonFile: String): Model.EbookSKB {
        var ebook: Model.EbookSKB? = null

        try {

            val f = File(jsonFile)

            val json = Json {
                ignoreUnknownKeys = true // Ignore fields not present in the Kotlin model
                isLenient = true         // Allows lenient parsing
                prettyPrint = true       // Pretty-print output JSON
                useAlternativeNames = false
            }

            ebook =  json.decodeFromString<Model.EbookSKB>(f.readText())

        }catch (e: Exception) {
            e.printStackTrace()
        }

        if (ebook == null) {
            throw Exception("Nooo way, failed to parse json file to model!")
        }

        var index = 1;
        ebook.parts.forEach {part ->
            part.title = ebook.title.uppercase()
            part.index = String.format("%02d", index)

            var chr = 66
            part.chapters.forEach {chapter ->
                chapter.index = chr.toChar().toString()
                chr++
            }

            index++
        }

        return ebook
    }
}
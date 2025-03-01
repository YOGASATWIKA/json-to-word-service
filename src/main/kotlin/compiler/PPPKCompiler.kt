package id.pande.compiler

import com.deepoove.poi.XWPFTemplate
import com.deepoove.poi.config.Configure
import com.deepoove.poi.data.DocxRenderData
import com.deepoove.poi.data.Includes
import id.pande.policy.MyTextRenderPolicy
import kotlinx.serialization.json.Json
import java.io.File

data class Parser(
    val title: String,
    val technical: DocxRenderData,
    val interview: DocxRenderData,
)

open class PPPKCompiler(): EbookCompiler {

    override fun compile(source: String): String {

        val ebook = getDataFromJSON(source)

        val data = Parser(
            title = ebook.ebookTitle,
            technical = Includes.ofLocal("./templates/pppk/part.docx").setRenderModel(ebook.technical.parts).create(),
            interview = Includes.ofLocal("./templates/pppk/part.docx").setRenderModel(ebook.interview.parts).create(),
        )

        val config = Configure.
            builder().
            bind("expanded", MyTextRenderPolicy()).
            bind("content",  MyTextRenderPolicy()).
            build()

        val pathOutput = "./output/pppk/${ebook.ebookTitle}.docx"

        try {
            XWPFTemplate.compile("./templates/pppk/template.docx", config).render(data).writeToFile(pathOutput)

        }catch (e: Exception) {
            e.printStackTrace()
        }

        return pathOutput
    }

    private fun getDataFromJSON(jsonFile: String): Model.Ebook {
        var ebook: Model.Ebook? = null

        try {

            val f = File(jsonFile)

            val json = Json {
                ignoreUnknownKeys = true // Ignore fields not present in the Kotlin model
                isLenient = true         // Allows lenient parsing
                prettyPrint = true       // Pretty-print output JSON
                useAlternativeNames = false
            }

            ebook =  json.decodeFromString<Model.Ebook>(f.readText())

        }catch (e: Exception) {
            e.printStackTrace()
        }

        if (ebook == null) {
            throw Exception("Nooo way, failed to parse json file to model!")
        }

        var globalIndex = 8

        // Add some data
        ebook.technical.parts.forEach {part ->
            part.title = "TEKNIS ${ebook.ebookTitle.uppercase()}"
            part.index = String.format("%02d", globalIndex)

            var chr = 66
            part.chapters.forEach {chapter ->
                chapter.index = chr.toChar().toString()
                chr++
            }

            globalIndex++
        }
        // Add some data
        ebook.interview.parts.forEach {part ->
            part.title = "WAWANCARA ${ebook.ebookTitle.uppercase()}"
            part.index = String.format("%02d", globalIndex)

            var chr = 66
            part.chapters.forEach {chapter ->
                chapter.index = chr.toChar().toString()
                chr++
            }
        }

        return ebook
    }
}
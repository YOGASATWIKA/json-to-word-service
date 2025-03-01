package id.pande.compiler

import com.deepoove.poi.XWPFTemplate
import com.deepoove.poi.config.Configure
import com.deepoove.poi.data.DocxRenderData
import com.deepoove.poi.data.Includes
import id.pande.policy.MyTextRenderPolicy
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

open class PPPK2Compiler(): EbookCompiler {

    data class ParserPPPK2(
        var title: String,
        val technical: DocxRenderData,
        val interview: DocxRenderData,
        var a: Int,
        var b: Int,
        var c: Int,
        var d: Int,
        var e: Int,
        var f: Int,
        var g: Int,
    )

    override fun compile(source: String): String {

        val data = getDataFromJSON(source)

        val config = Configure.
            builder().
            bind("expanded", MyTextRenderPolicy()).
            bind("content",  MyTextRenderPolicy()).
            build()

        val pathOutput = "./output/pppk/${data.title}.docx"

        try {
            XWPFTemplate.compile("./templates/pppk-2/template.docx", config).render(data).writeToFile(pathOutput)

        }catch (e: Exception) {
            e.printStackTrace()
        }

        return pathOutput
    }

    private fun getDataFromJSON(jsonFile: String): ParserPPPK2 {

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
            throw Exception("Nooo way, failed to parse json file to model! $jsonFile")
        }



        var globalIndex = 1

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


        val a = globalIndex + 0
        val b = globalIndex + 1
        val c = globalIndex + 2
        val d = globalIndex + 3
        val e = globalIndex + 4
        val f = globalIndex + 5
        val g = globalIndex + 6

        globalIndex+= 7


        // Add some data
        ebook.interview.parts.forEach {part ->
            part.title = "WAWANCARA ${ebook.ebookTitle.uppercase()}"
            part.index = String.format("%02d", globalIndex)

            var chr = 66
            part.chapters.forEach {chapter ->
                chapter.index = chr.toChar().toString()
                chr++
            }

            globalIndex++
        }

        return ParserPPPK2(
            title = ebook.ebookTitle.uppercase(Locale.getDefault()),
            technical = Includes.ofLocal("./templates/pppk-2/part.docx").setRenderModel(ebook.technical.parts).create(),
            interview = Includes.ofLocal("./templates/pppk-2/part.docx").setRenderModel(ebook.interview.parts).create(),
            a = a,
            b = b,
            c = c,
            d = d,
            e = e,
            f = f,
            g = g
        )
    }
}
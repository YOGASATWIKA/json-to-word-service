package id.pande.compiler

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

class Model {

    @Serializable
    data class Ebook(
        @SerialName("ebook_title")
        val ebookTitle: String,
        val technical: Wrapper,
        val interview: Wrapper
    )

    @Serializable
    data class EbookSKB(
        var title: String,
        val parts: List<Part>,
    )

    @Serializable
    data class Wrapper(
        var title: String,
        val parts: List<Part>,
    )

    @Serializable
    data class Part(
        var title: String? = "",
        var index: String? = "",
        val subject: String,
        val introductions: List<String>,
        val urgencies: List<String>,
        val chapters: List<Chapter>
    )

    @Serializable
    data class Chapter(
        var index: String? = "",
        val title: String,
        @SerialName("base_competitions")
        val baseCompetitions: List<String>,
        @SerialName("trigger_questions")
        val triggerQuestions: List<String>,
        val materials: List<Material>,
        val conclusion: String,
        val reflections: List<String>
    )

    @Serializable
    data class Material(
        val title: String,
        val short: String,
        val details: List<Detail>
    )

    @Serializable
    data class Detail(
        val content: String,
        val expanded: String? = null,
        @SerialName("expand_chunks")
        val expandChunks: List<String>? = null
    )

}
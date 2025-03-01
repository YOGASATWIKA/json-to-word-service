package id.pande

import id.pande.compiler.PPPK2Compiler
import id.pande.compiler.PPPKCompiler
import id.pande.compiler.SKBCompiler
import java.io.File
import kotlinx.coroutines.*
import java.nio.file.Paths

fun main()  {

//    val directory = File("./sources/fungsional")
//
//    generate(directory)


//    PPPK2Compiler().compile("./sources/fungsional/ADMINISTRATOR KESEHATAN AHLI PERTAMA.json")

    SKBCompiler().compile("./sources/skb.json")
}

fun generate(directory: File) {
    if(directory.exists() && directory.isDirectory) {
        for (file in directory.listFiles()!!) {
            if (file.extension == "json") {
                println(PPPK2Compiler().compile(file.absolutePath))
            }
        }
    }
}


package com.amindforlanguages.learningwithtextsandroid

import android.content.Context
import arrow.core.*
import org.jsoup.Jsoup
import org.intellij.markdown.*
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

typealias Markdown = String

object TextProcessor {


    var jscode = """
<script language="javascript">
function wordClicked(word, id, lang) {
    interface.callWithArg(word, id, lang);
    return false;
}

function startMyTimer(word, id, lang) {
    interface.startTimer(word, id, lang)
    return false;
}

function updateTermClass(term, newclass, oldclass) {
    console.log(term)
    var ts = document.getElementsByClassName(term);
    console.log(ts)
    for (var i = 0; i < ts.length; i++) {
        var t = ts[i];
        t.classList.remove(oldclass);
        t.classList.add(newclass);
    }
}</script>
        """

    private fun wrapWord( x :String, myclass : Int, id : Int, lang : String) : String {
        //  onclick="nukeClick();"
        return "<span class=\"${x.toLowerCase()} ${getCssClass(myclass)}\" onmouseup=\"wordClicked('$x', $id, '$lang');\"  ontouchstart=\"startMyTimer('$x', $id, '$lang');\" >$x</span>"
    }

    var css = """
        <style>
         .one { background-color: orange; }
         .two { background-color: yellow; }
         .three { background-color: blue; }
         .four { background-color: lightblue; }
         .five { background-color: lightgreen; }
         .ignore { color: darkslategray; }
         </style>
    """
    val reg = "[.,/#!\$%^&*;:{}=\\-_`~()\\[\\]]".toRegex()
    val reg2 = "[.,/#!\$%^&*;:{}=\\-_`~()\\[\\]><]".toRegex()

    fun removePunct(word :String) : String  = word.replace(reg, "")

    fun replacePunctinMD(str : String, rep : String) = str.replace(reg2, rep)

    //TODO add convertor to html
    //TODO remove jsoup stuff
    fun toHtml(x : String, lang : String,  ctx : Context) : String {

        val db = DBManager.getInstance(ctx)
        var out = x
        out = out.replace("<body>", "$jscode\n$css\n<body>")
        val words = getAllTextNodes(Jsoup.parse(x).body())
        for (w in words) {
            val matches = db.getWordsForHtml(lang, w.toLowerCase())
            var wclass = 0
            var wid = -1
            when (matches) {
                is Some -> {
                    val m = matches.t[0]
                    wclass = m.myclass
                    wid = m.id
                }
            }

            val nukeit = removePunct(w)
            if (nukeit != "") {
                out = out.replace("\\b$nukeit\\b".toRegex(), wrapWord(w, wclass, wid, lang))
            }
        }
        return out
    }

    fun stripHtmlTags(md : Markdown) : String = Jsoup.parse(md).text()

    fun getWordsFromMd(md : Markdown) : List<String>  =  replacePunctinMD(stripHtmlTags(md), " ").split("\\b".toRegex()).toSet().filter { !it.trim().isNullOrBlank() }

    fun getMdWords(md : Markdown) : List<String> = replacePunctinMD(md, " ").split("\\b".toRegex()).filter { !it.trim().isNullOrBlank() }

    fun countMdWords (md: Markdown) : Int = getMdWords(md).count()

    //fun nukeMDCodes(md : Markdown) : String = md.replace("[#*]".toRegex(), "")

    fun convertMdToHtml(md : Markdown) : String {
        val flavor = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavor).buildMarkdownTreeFromString(md)
        return HtmlGenerator(md, parsedTree, flavor).generateHtml()
    }

    fun toHtmlFromMD(x : String, lang : String,  ctx : Context) : String {
        val db = DBManager.getInstance(ctx)
        var out = convertMdToHtml(x)
        val words = getWordsFromMd(x)

        for (w in words) {
            val matches = db.getWordsForHtml(lang, w.toLowerCase())
            var wclass = 0
            var wid = -1
            when (matches) {
                is Some -> {
                    val m = matches.t[0]
                    wclass = m.myclass
                    wid = m.id
                }
            }

            val nukeit = removePunct(w)
            if (nukeit != "") {
                out = out.replace("\\b$nukeit\\b".toRegex(), wrapWord(w, wclass, wid, lang))
            }
        }

        return out.replace("<body>", "$jscode\n$css\n<body>")

    }
//TODO add thing to split long texts into shorter files

    fun getCssClass(x : Int) : String {
        return when (x) {
            0 -> "unknown"
            1 -> "one"
            2 -> "two"
            3 -> "three"
            4 -> "four"
            5 -> "five"
            6 -> "known"
            -1 -> "ignore"
            else -> "unknown"
        }
    }

    private fun getAllTextNodes(node : org.jsoup.nodes.Node) : MutableSet<String> {
        val out = mutableSetOf<String>()
        for (n : org.jsoup.nodes.Node in node.childNodes()) {
            if (n is org.jsoup.nodes.TextNode && !(n.text().isBlank())) {
                val y = n.text().trim().split(" ").filter { !(it.trim() == "") }
                when {
                    y.count() > 1 -> out.addAll(y)
                    y.count() < 1 -> return out
                    else -> out.add(y[0])
                }
            } else {
                out.addAll(getAllTextNodes(n))
            }
        }
        return out
    }
}
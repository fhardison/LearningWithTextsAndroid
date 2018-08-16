package com.amindforlanguages.learningwithtextsandroid

import android.content.Context
import org.jsoup.Jsoup


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
        return "<span class=\"${x.toLowerCase()} ${getCssClass(myclass)}\" onmouseup=\"wordClicked('$x', $id, '$lang');\"  onmousedown=\"startMyTimer('$x', $id, '$lang');\" >$x</span>"
    }

    var css = """
        <style>
         .one { background-color: orange; }
         .two { background-color: yellow; }
         .three { background-color: blue; }
         .four { background-color: lightblue;}
         .five { background-color: green;}
         .ignore { background-color: indigo; color: white;}
         </style>
    """

    fun toHtml(x : String, lang : String,  ctx : Context) : String {
        val db = DBManager.getInstance(ctx)
        var out = x
        val words = getAllTextNodes(Jsoup.parse(x).body())
        for ( w in words) {
            val matches = db.getWords(lang, w.toLowerCase())
            var wclass = 0
            var wid = -1
            if (matches != null) {
                wclass = matches[0].myclass
                wid = matches[0].id
            }
            out = out.replace("\\b$w\\b".toRegex(), wrapWord(w, wclass, wid, lang))
        }
        return out
    }

    fun updateTermInHtml(html :String, w : String, wclass :Int, wid :Int, lang :String) : String {
        val h = Jsoup.parse(html)

        val matches = h.select("span[data-id=$wid]")

        for (m in matches) {
            m.removeAttr("class")
            m.addClass(getCssClass(wclass))
        }
        return h.outerHtml()
        //return html.replace("\\b$w\\b".toRegex(), wrapWord(w, wclass, wid, lang))
    }

    /*
        private fun getClass(word :String, lang : String, db : DBManager) : Int {
            //TODO figure out DB stuff
            val words = db.getWords(lang, word)
            if (words != null){
                return words[0].myclass
            }
            return 0
        }
    */
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

    private fun getAllTextNodes(node : org.jsoup.nodes.Node) : MutableList<String> {
        val out = mutableListOf<String>()
        for (n : org.jsoup.nodes.Node in node.childNodes()) {
            if (n is org.jsoup.nodes.TextNode && !(n.text().isBlank())) {
                val y = n.text().trim().split(" ")
                if (y.count() > 1) {
                    out.addAll(y)
                } else {
                    out.add(y[0])
                }
            } else {
                out.addAll(getAllTextNodes(n))
            }
        }
        return out.toSet().toMutableList()
    }
}
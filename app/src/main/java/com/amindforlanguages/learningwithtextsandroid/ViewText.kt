package com.amindforlanguages.learningwithtextsandroid
import android.annotation.SuppressLint
import android.app.FragmentManager
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.Toast
import org.jsoup.Jsoup
import java.io.*
import java.util.*

class ViewText : AppCompatActivity() {


    private class JavascriptInterface(ctx : Context, fragManager : FragmentManager) {

        val myctx : Context = ctx
        var t : Timer? = null
        var word : String = ""
        var myid : Int = -1
        var lang : String = ""

        @android.webkit.JavascriptInterface
        fun startTimer(x : String, id : Int, l : String) {
            word = x
            myid = id
            lang = l

            /*
            t = Timer("my timer", false)
            t!!.schedule(500) {
                timerFinished = true
                showDialog(x, id, l)
            } */
        }

        @android.webkit.JavascriptInterface
        fun callWithArg(x : String, id : Int, l : String){
            word = x
            myid = id
            lang = l
            showWord(x, id, l)
        }


        fun showWord(x: String, id : Int, lang :String) {
            if (id == -1) {
                // showDialog(x, id, lang)
                Toast.makeText(myctx, "$x not found in term list", Toast.LENGTH_SHORT).show()
            } else {
                val db = DBManager.getInstance(myctx)
                val match = db.getWordById(id)
                if (match != null) {
                    Toast.makeText(myctx, "${match.word} = ${match.gloss}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(myctx, "$x not found in term list", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /*
        @android.webkit.JavascriptInterface
        fun showDialog(x :String, id: Int, lang :String) {
            val dialog = TermEdit.newInstance(lang, id, x)
            dialog.show(fragMan, "term_edit")
        } */
    }


    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_text)


        val wv = this.findViewById<WebView>(R.id.textwebview)
        // disable long press so that the javascript will work.

        wv.settings.javaScriptEnabled = true

        val fragManager = this.fragmentManager
        val jsi = JavascriptInterface(this, fragManager)

        wv.addJavascriptInterface(jsi, "interface")
        wv.isLongClickable = true
        wv.isClickable = true
        val test = "<html>" + TextProcessor.jscode + TextProcessor.css + "<body><h1>Hi</h1><b>Click me</b></body></html>"
        val update = {term: String, id: Int, cl: Int, l: String, oldcl :Int -> updateHtml(wv, term,id, cl, l, oldcl); -1}

        wv.setOnLongClickListener {
            val dialog = TermEdit.newInstance(jsi.lang, jsi.myid, jsi.word, update)
            dialog.show(fragManager, "my dialog")
            true
        }
        wv.setOnClickListener {
            jsi.showWord(jsi.word, jsi.myid, jsi.lang)
            //true
        }

        wv.loadData(TextProcessor.toHtml(test, "Nederlands", this), "text/html", null)

    }


    fun updateHtml(wv: WebView, term: String, id: Int, myclass: Int, lang: String, oldcl :Int) {

        val cmd = "javascript:updateTermClass('${term.toLowerCase()}', '${TextProcessor.getCssClass(myclass)}', '${TextProcessor.getCssClass(oldcl)}');"
        wv.evaluateJavascript(cmd, null)
    }

}


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
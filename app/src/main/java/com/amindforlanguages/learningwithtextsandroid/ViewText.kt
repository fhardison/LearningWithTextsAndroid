package com.amindforlanguages.learningwithtextsandroid
import android.annotation.SuppressLint
import android.app.FragmentManager
import android.content.Context
import android.content.res.Resources
import android.net.Uri
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
                val message = db.getWordById(id).fold({"$x not found in term list"},{ "${it.word} = ${it.gloss}"})
                Toast.makeText(myctx, message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_text)
        val args = intent.extras

        if (args != null) {
            val fpath = args.getString("FPATH")


            val wv = this.findViewById<WebView>(R.id.textwebview)
            // disable long press so that the javascript will work.

            wv.settings.javaScriptEnabled = true

            val fragManager = this.fragmentManager
            val jsi = JavascriptInterface(this, fragManager)

            wv.addJavascriptInterface(jsi, "interface")
            wv.isLongClickable = true
            wv.isClickable = true

            val update = { term: String, id: Int, cl: Int, l: String, oldcl: Int -> updateHtml(wv, term, id, cl, l, oldcl); -1 }

            wv.setOnLongClickListener {
                val dialog = TermEdit.newInstance(jsi.lang, jsi.myid, jsi.word, update)
                dialog.show(fragManager, "my dialog")
                true
            }
            wv.setOnClickListener {
                jsi.showWord(jsi.word, jsi.myid, jsi.lang)
                //true
            }

            //val test = "<html>" + TextProcessor.jscode + TextProcessor.css + "<body><h1>Hi</h1><b>Click me</b></body></html>"

            wv.loadData(TextProcessor.toHtmlFromMD(getText(fpath), "Nederlands", this), "text/html", null)
        }
    }


    private fun updateHtml(wv: WebView, term: String, id: Int, myclass: Int, lang: String, oldcl :Int) {

        val cmd = "javascript:updateTermClass('${term.toLowerCase()}', '${TextProcessor.getCssClass(myclass)}', '${TextProcessor.getCssClass(oldcl)}');"
        wv.evaluateJavascript(cmd, null)
    }

    private fun getText(path :String) : String {
        val uri = Uri.parse(path)
        return contentResolver.openInputStream(uri).bufferedReader().use {it.readText()}
        //return File(uri.toString()).bufferedReader().use { it.readText() }

    }
}


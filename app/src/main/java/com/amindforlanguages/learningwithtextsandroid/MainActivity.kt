package com.amindforlanguages.learningwithtextsandroid

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val showtext = this.findViewById<Button>(R.id.showtext);

        showtext.setOnClickListener {
            val show = Intent(this, TextsList::class.java)
            show.putExtra("LANG", "Nederlands")
            startActivity(show)
        }

        val showTerms = this.findViewById<Button>(R.id.showterms)

        showTerms.setOnClickListener {
            val terms = Intent(this, AddText::class.java)
            startActivity(terms)
        }

        val showLangs = this.findViewById<Button>(R.id.showlangs)

        showLangs.setOnClickListener {
            val langs = Intent(this, LanguageList::class.java)
            startActivity(langs)
        }


        val db = DBManager.getInstance(this)

        val t = Term(1, "click",2, "klik", "V", "Click me", "used for computers", "Nederlands")
        db.insertTerm(t)

        //db.insertLanguage("Nederlands")
        //db.insertLanguage("English")
    }
}

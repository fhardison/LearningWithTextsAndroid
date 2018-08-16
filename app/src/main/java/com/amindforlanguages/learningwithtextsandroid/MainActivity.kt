package com.amindforlanguages.learningwithtextsandroid

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var showtext = this.findViewById<Button>(R.id.showtext);

        showtext.setOnClickListener {
            var show = Intent(this, ViewText::class.java)
            startActivity(show)
        }

        val db = DBManager.getInstance(this)

        val t = Term(1, "click",2, "klik", "V", "Click me", "used for computers", "Nederlands")
        db.insertTerm(t)

    }
}

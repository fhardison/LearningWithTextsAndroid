package com.amindforlanguages.learningwithtextsandroid

import android.app.Dialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.*
import arrow.core.getOrElse
import java.sql.SQLException

class LanguageList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_list)

        val addBtn = findViewById<Button>(R.id.add_language_btn)
        addBtn.setOnClickListener { addLanguageClick() }

        val langList = findViewById<ListView>(R.id.language_list)
        val db = DBManager.getInstance(this)
        val lnames = db.getLanguages().map { it.map { l -> l.name} }.getOrElse { listOf<String>() }

        val adaptor =  ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lnames)
        langList.adapter = adaptor
    }

    fun addLanguageClick() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Language")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT


        builder.setView(input)

        builder.setNegativeButton("Cancel") { dialog :DialogInterface, _ :Int -> dialog.cancel()}

        builder.setPositiveButton("Add") { dialog: DialogInterface, i: Int ->
            val db = DBManager.getInstance(this)
            try {
                db.addLanguage(input.text.toString())
                Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show()
            } catch (e: SQLException) {
                Toast.makeText(this, "Language already exists", Toast.LENGTH_SHORT).show()
            }
        }
        builder.create().show()
    }
}


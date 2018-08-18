package com.amindforlanguages.learningwithtextsandroid

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.activity_add_text.*


class AddText : AppCompatActivity() {

    private var filePath : Uri? = null
    private var langId : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_text)
        setSupportActionBar(toolbar)

        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT

        val selectButton = findViewById<Button>(R.id.pick_text_button)
        selectButton.setOnClickListener { _ -> startActivityForResult(Intent.createChooser(intent, "Select a file"), 213)}

        val langBtn = findViewById<Button>(R.id.pick_text_pick_language)
        langBtn.setOnClickListener { _ -> pickLanguage()}
        val textTitle = findViewById<EditText>(R.id.add_text_title)

        val cancelBtn = findViewById<Button>(R.id.pick_text_cancel)
        cancelBtn.setOnClickListener { _ -> this.finish() }

        val saveBtn = findViewById<Button>(R.id.pick_text_ok)
        saveBtn.setOnClickListener { _ -> saveText(this.applicationContext,textTitle.text.toString(), filePath.toString(), langId ) }


    }

    private fun saveText(ctx : Context, textName : String, fpath :String, langId :Int ) {

        val db = DBManager.getInstance(ctx)

        db.saveText(textName, fpath, langId)
        Toast.makeText(ctx, "Text Saved", Toast.LENGTH_SHORT).show()

        this.finish()
    }

    fun pickLanguage() {

        val dialog = LanguageDialog()
        val langTV = findViewById<TextView>(R.id.pick_text_language)
        dialog.onSelected = {s -> langTV.text = s.name; langId = s.id; -1 }
        dialog.show(fragmentManager, "Language Picker")

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                filePath = data.data
                findViewById<TextView>(R.id.pick_text_path).text = filePath.toString()
            }
        } else {
            Toast.makeText(this, "No File selected", Toast.LENGTH_SHORT).show()
        }
    }
}


class LanguageDialog : DialogFragment() {

    var onSelected : ((Language) -> Int) = {l -> -1 }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Pick Language")

        val db = DBManager.getInstance(activity)

        val ls = db.getLanguages()
        val lnames = ls.map { l -> l.name }
        val inflator = activity.layoutInflater
        val view  = inflator.inflate(R.layout.list_view_dialog, null)
        val lv = view.findViewById<ListView>(R.id.listView1)
        builder.setView(view)
       val adaptor =  ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_1, lnames)
        lv.adapter = adaptor
        lv.setOnItemClickListener { _, _, i, l -> onSelected(ls[i]); dismiss(); }
        //builder.setAdapter(adaptor) { dialog : DialogInterface, which: Int -> onSelected(ls[which]); dialog.dismiss();}


        return builder.create()
       // dialog.listView.adapter = adaptor
       // dialog.listView.setOnItemClickListener { _, _, i, l -> onSelected(ls[i]); dismiss(); }
    }
}
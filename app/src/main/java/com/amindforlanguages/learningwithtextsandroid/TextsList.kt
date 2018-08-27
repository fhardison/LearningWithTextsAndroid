package com.amindforlanguages.learningwithtextsandroid

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import arrow.core.*

class TextsList : AppCompatActivity() {

    private lateinit var recyclerView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texts_list)
        val args = intent.extras
        if (args != null) {
            val lang = args.getString("LANG")
            this.recyclerView = findViewById(R.id.my_recycler_view)
            val db = DBManager.getInstance(this)
            val t = db.getTexts(lang)
            when (t) {
                is Some -> {
                    val texts = t.t
                    this.recyclerView.adapter = TextAdaptor(this, texts)
                    this.recyclerView.setOnItemClickListener { _, _, i, _ ->
                        val selected = texts[i]
                        val intent = Intent(this, ViewText::class.java)
                        intent.putExtra("FPATH", selected.path)
                        intent.putExtra("TNAME", selected.name)
                        intent.putExtra("TLANG", selected.lang)
                        startActivity(intent)
                    }
                    this.recyclerView.isLongClickable = true
                    this.recyclerView.onItemLongClickListener = AdapterView.OnItemLongClickListener{ _, _, i, _ ->
                        val selected = texts[i]
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Warning")
                        builder.setMessage("Delete ${selected.name}?")
                        builder.setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                            db.removeText(selected.id)
                            Toast.makeText(this, "Text deleted", Toast.LENGTH_SHORT).show()
                            recyclerView.invalidate()
                        }
                        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _ : Int -> dialog.cancel() }
                        builder.create().show()
                        true
                    }
                }
                is None -> Toast.makeText(this, "No texts found", Toast.LENGTH_SHORT).show()
            }
        }

    }
}


class TextAdaptor(ctx : Context,
                  private val data : List<Text>) : BaseAdapter() {

    private val inflator :LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflator.inflate(R.layout.my_text_view, parent, false)

        val textName = rowView.findViewById<TextView>(R.id.text_name_holder)
        val textLang = rowView.findViewById<TextView>(R.id.text_lang_holder)

        val t = getItem(position) as Text
        textName.text = t.name
        textLang.text = t.lang


        return rowView
    }

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }

}
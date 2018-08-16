package com.amindforlanguages.learningwithtextsandroid

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast

class TermEdit : DialogFragment() {
    companion object {
        //term: String, id: Int, cl: Int, l: String
        fun newInstance(lang :String, id:Int, word : String, update : (String, Int, Int, String, Int) -> Int) : DialogFragment {
            val dialog = TermEdit()

            val args= Bundle()
            args.putString("lang", lang)
            args.putInt("id", id)
            args.putString("word", word)
            dialog.arguments = args
            dialog.updateHtml = update
            return dialog
        }
    }

    var lang :String? = null
    var myid :Int? = -1
    var myclass :Int? = -1
    var myOldClass : Int? = -1
    var word : String = ""
    var updateHtml : ((String, Int, Int, String, Int) -> Int)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (this.arguments != null) {
            val args = this.arguments
            lang = args.getString("lang")
            myid = args.getInt("id")
            word = args.getString("word")
            if (myid == null) {
                myid = -1
            }
        }


        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater


        builder.setTitle("Term")

        val content = inflater.inflate(R.layout.edit_term_view, null)

        val term = content.findViewById<EditText>(R.id.edit_term_term)
        term.setText(word)

        if (myid!! > -1) {
            val db = DBManager.getInstance(activity)
            Log.d("DBmanager", "Dialog fetching term with id of ${myid!!}")
            val match = db.getWordById(myid!!)
            if (match != null) {
                val gl = content.findViewById<EditText>(R.id.edit_term_gloss)
                val exe = content.findViewById<EditText>(R.id.edit_term_exe)
                val notes = content.findViewById<EditText>(R.id.edit_term_notes)
                val pos = content.findViewById<EditText>(R.id.edit_term_pos)
                gl.setText(match.gloss)
                exe.setText(match.examples)
                notes.setText(match.notes)
                pos.setText(match.pos)
                myOldClass = match.myclass
                Log.d("EditTermDialog", "Dialog match had class of ${match.myclass}")
                val cl = getRadioEdit(match.myclass)
                if ( cl > 0) {
                    val classrb = content.findViewById<RadioButton>(cl)
                    classrb.isChecked = true
                }
            }
        }


        val rb1 = content.findViewById<RadioButton>(R.id.edit_text_cl_1)
        val rb2 = content.findViewById<RadioButton>(R.id.edit_text_cl_2)
        val rb3 = content.findViewById<RadioButton>(R.id.edit_text_cl_3)
        val rb4 = content.findViewById<RadioButton>(R.id.edit_text_cl_4)
        val rb5 = content.findViewById<RadioButton>(R.id.edit_text_cl_5)
        val ig = content.findViewById<RadioButton>(R.id.edit_text_cl_ignore)

        val rbs = listOf(rb1, rb2, rb3, rb4, rb5, ig)

        rb1.setOnClickListener {v -> onClassChanged(v, rbs)}
        rb2.setOnClickListener {v -> onClassChanged(v, rbs)}
        rb3.setOnClickListener {v -> onClassChanged(v, rbs)}
        rb4.setOnClickListener {v -> onClassChanged(v, rbs)}
        rb5.setOnClickListener {v -> onClassChanged(v, rbs)}
        ig.setOnClickListener {v -> onClassChanged(v, rbs)}




        val onSave = {dialog: DialogInterface, id: Int -> saveEdit(dialog, id, myid!!, lang!!, myOldClass!!) }
        val oncancel = { dialog :DialogInterface, _:Int -> dialog.cancel()}

        builder.setView(content)
                .setPositiveButton("Save", onSave)
                .setNegativeButton("Cancel", oncancel)


        return builder.create()
    }

    private fun saveEdit(dialog: DialogInterface, id: Int, myid:Int, lang: String, oldcl :Int) {
        val term = this.dialog.findViewById<EditText>(R.id.edit_term_term).text.toString()
        val gl = this.dialog.findViewById<EditText>(R.id.edit_term_gloss).text.toString()
        val notes = this.dialog.findViewById<EditText>(R.id.edit_term_notes).text.toString()
        val exe = this.dialog.findViewById<EditText>(R.id.edit_term_exe).text.toString()
        val pos = this.dialog.findViewById<EditText>(R.id.edit_term_pos).text.toString()

        val t = Term(myid, term, myclass!!,gl,pos,exe,notes,lang)
        Log.d("EditTermDialog", "Saving term with class of $myclass!!")
        val db = DBManager.getInstance(activity)
        db.updateWord(t)

        updateHtml?.invoke(term, myid, myclass!!, lang, oldcl)
        Toast.makeText(activity,"Saved", Toast.LENGTH_SHORT)
    }

    fun onClassChanged(view: View, rbs : List<RadioButton>) {
        val checked = (view as RadioButton).isChecked
        val unCheck = rbs.filter { x -> x.id != view.id }
        unCheck.forEach { x-> x.isChecked = false }
        when(view.id) {
            R.id.edit_text_cl_1 -> if (checked) {
                myclass = 1
            }
            R.id.edit_text_cl_2 -> if (checked) {
                myclass = 2
            }
            R.id.edit_text_cl_3 -> if (checked) {
                myclass = 3
            }
            R.id.edit_text_cl_4 -> if (checked) {
                myclass = 4
            }
            R.id.edit_text_cl_5 -> if (checked) {
                myclass = 5
            }
            R.id.edit_text_cl_ignore -> if (checked) {
                myclass = -1
            }
        }
    }

    fun getRadioEdit(mycl :Int) : Int {
        return when(mycl) {
            1 -> R.id.edit_text_cl_1
            2 -> R.id.edit_text_cl_2
            3 -> R.id.edit_text_cl_3
            4 -> R.id.edit_text_cl_4
            5 -> R.id.edit_text_cl_5
            -1 -> R.id.edit_text_cl_ignore
            else -> -1
        }
    }
}
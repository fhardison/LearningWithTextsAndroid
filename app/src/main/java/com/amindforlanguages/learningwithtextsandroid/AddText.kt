package com.amindforlanguages.learningwithtextsandroid

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import arrow.core.*
import kotlinx.android.synthetic.main.activity_add_text.*
import com.overzealous.remark.Remark
import java.io.File
import java.io.FileNotFoundException
import java.util.jar.Manifest


class AddText : AppCompatActivity() {

    private var filePath : Uri? = null
    private var langId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_text)
        setSupportActionBar(toolbar)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(this, "No permission to write sotrage", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(this, "No permission to write sotrage", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 124)
        }

        val intent = Intent()
        intent.type = "text/html"
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val selectButton = findViewById<Button>(R.id.pick_text_button)
        selectButton.setOnClickListener { _ -> startActivityForResult(Intent.createChooser(intent, "Select a file"), 213)}

        val langBtn = findViewById<Button>(R.id.pick_text_pick_language)
        langBtn.setOnClickListener { _ -> pickLanguage()}
        val textTitle = findViewById<EditText>(R.id.add_text_title)

        val cancelBtn = findViewById<Button>(R.id.pick_text_cancel)
        cancelBtn.setOnClickListener { _ -> this.finish() }

        val enterTB = findViewById<EditText>(R.id.insert_text_content_box)


        val saveBtn = findViewById<Button>(R.id.pick_text_ok)
        saveBtn.setOnClickListener { _ ->
            if (!enterTB.text.toString().isNullOrBlank()){
                saveTextFromString(this.applicationContext,cleanName(textTitle.text.toString().trim()), enterTB.text.toString(), langId )
            }
            else {
            saveText(this.applicationContext,textTitle.text.toString().trim(), filePath.toString(), langId )} }


    }
    val cleanName = {x : String -> x.replace("%20", " ")}
    private fun saveTextFromString (ctx : Context, textName : String, content :String, langId :String ) {

        val db = DBManager.getInstance(ctx)
        val message = makeNewFile(textName).map { it.bufferedWriter().use { it.write(content)  }; it.toURI().toString() }
                .map {db.saveText(textName, listOf(it), langId)}
                .fold({"Text not saved"}, {"Text Saved"})
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
        this.finish()
    }

    private fun saveText(ctx : Context, textName : String, fpath :String, langId :String ) {

        val db = DBManager.getInstance(ctx)
        val message = convertToMarkdownAndGetPath(fpath)
                .map {db.saveText(textName, it, langId)}
                .fold({"Text not saved"}, {"Text Saved"})
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()

        this.finish()
    }

    private fun splitToFiles(md : String) : List<String> {
        val words = TextProcessor.getMdWords(md)
        return words.chunked(500).map { it.joinToString(" ")}
    }

    private fun saveFile(content : String, fpath: String, part : Int) : String {
       return getFile(fpath, part).map {
            it.printWriter().use { out -> out.print(content) };
           it
        }.fold({""}, { return if (it.toURI() != null) it.toString() else "" })
    }

    private fun convertToMarkdownAndGetPath(fpath :String) : Option<List<String>> {
        val remark = Remark()
        val content= Try { contentResolver.openInputStream(Uri.parse(fpath)).bufferedReader().use {it.readText()}}.toEither()
                .flatMap { Right(remark.convert(it)) }

        return when (content) {
            is Either.Right -> {
                val wordCount = TextProcessor.countMdWords(content.b)

                if (wordCount > 500) {
                    val chunks = splitToFiles(content.b)
                    var chunkNumber = 0

                    val fNames = chunks.map {
                        chunkNumber += 1;
                        saveFile(it, fpath, chunkNumber)}.filter { !it.isNullOrEmpty() }

                    return Some(fNames)
                } else {
                    return Some(listOf(saveFile(content.b, fpath, 1)))
                }


            }
            is Either.Left -> None
        }
    }

    private fun getFile(fpath : String) : Option< File> {
        val sdMain = File(Environment.getExternalStorageDirectory().toString() + "/Download")
        var dirExists = true
        if (!sdMain.exists()){
            dirExists = sdMain.mkdirs()
        }

        if (dirExists) {
            return Uri.parse(fpath)
                    .getRealPath(contentResolver)
                    .map{ it.replace(".html", ".md")}
                    .map { File(sdMain, it) }
        }
        return None //Left(FileNotFoundException("Could not create output for $fpath"))
    }

    private fun makeNewFile(textName : String) : Option<File> {
        val sdMain = File(Environment.getExternalStorageDirectory().toString() + "/Download")
        var dirExists = true
        if (!sdMain.exists()){
            dirExists = sdMain.mkdirs()
        }
        return if (dirExists) {
            val f = File(sdMain, "${textName.trim()}.md")
//            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//            contentResolver.takePersistableUriPermission(Uri.parse(f.toURI().toString()), takeFlags)
           Some(f)
        } else { None }
    }

    private fun getFile(fpath : String, part : Int) : Option< File> {
        val sdMain = File(Environment.getExternalStorageDirectory().toString() + "/Download")
        var dirExists = true
        if (!sdMain.exists()){
            dirExists = sdMain.mkdirs()
        }

        if (dirExists) {
            return Uri.parse(fpath)
                    .getRealPath(contentResolver)
                    .map{ it.replace(".html", "_$part.md")}
                    .map { File(sdMain, it) }
        }
        return None //Left(FileNotFoundException("Could not create output for $fpath"))
    }
    fun pickLanguage() {

        val dialog = LanguageDialog()
        val langTV = findViewById<TextView>(R.id.pick_text_language)
        dialog.onSelected = {s -> langTV.text = s.name; langId = s.name; -1 }
        dialog.show(fragmentManager, "Language Picker")

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                filePath = data.data
                val takeFlags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(filePath, takeFlags)
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
        val db = DBManager.getInstance(activity)
        val langs = db.getLanguages()

        when (langs) {
            is Some -> {
                builder.setMessage("Pick Language")
                val ls = langs.t
                val lnames = ls.map { l -> l.name }
                val inflator = activity.layoutInflater
                val view  = inflator.inflate(R.layout.list_view_dialog, null)
                val lv = view.findViewById<ListView>(R.id.listView1)
                builder.setView(view)
                val adaptor =  ArrayAdapter<String>(activity,
                        android.R.layout.simple_list_item_1, lnames)
                lv.adapter = adaptor
                lv.setOnItemClickListener { _, _, i, l -> onSelected(ls[i]); dismiss(); }
            }
            is None -> {
                builder.setMessage("No languages found! Create a language first")
                builder.setNeutralButton("Ok", {dialog, i -> dialog.dismiss()})
            }
        }

        return builder.create()

    }
}

fun Uri.getRealPath(resolver : ContentResolver) : Option<String> {
    if(this.scheme.equals("file")) {
        return Some(this.path)
    }



    val cursor  = resolver.query(this, null, null, null, null)
    if (cursor.count > 0) {
        cursor.moveToFirst()
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val res = cursor.getString(nameIndex)
        cursor.close()
        return Some(res)
    }
    cursor.close()
    return None
}
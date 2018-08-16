package com.amindforlanguages.learningwithtextsandroid


import android.content.Context
import android.database.Cursor
import android.database.sqlite.*
import android.util.Log
import android.widget.Toast


class DBManager : SQLiteOpenHelper  {

    private constructor(ctx : Context) : super(ctx, dbname, null, 1) {
        //
    }

    companion object {
        private  var  instance : DBManager? = null

        fun getInstance(ctx: Context) : DBManager {
            if (instance == null) {
                instance = DBManager(ctx)
            }
            return instance!!
        }

        const val dbname = "com.amindforlanguage.lwtdroid.db"



        const val LANGUAGES_TABLE = "Languages"
        const val LANGUAGE_COL_NAME = "LanguageName"
        const val LANGUAGE_COL_ID = "LanguageId"

        const val TEXT_TABLE = "Texts"
        const val TEXT_COL_ID = "TextId"
        const val TEXT_COL_FILE_PATH = "TextFilePath"
        const val TEXT_COL_NAME = "TextName"
        const val TEXT_COL_LANGUAGE = "TextLanguage"
        const val TEXT_COL_WORD_COUNT = "TextWordCount"

        const val WORD_TABLE = "Terms"
        const val WORD_COL_TERM_ID = "WordId"
        const val WORD_COL_TERM = "WordTerm"
        const val WORD_COL_CLASS = "WordClass"
        const val WORD_COL_GLOSS = "WordGloss"
        const val WORD_COL_POS = "WordPOS"
        const val WORD_COL_EXAMPLE = "WordExample"
        const val WORD_COL_NOTES = "WordNotes"
        const val WORD_COL_LANGUAGE = "WordLanguage"
    }

    //val db : SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbname, null)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS $LANGUAGES_TABLE($LANGUAGE_COL_ID VARCHAR, $LANGUAGE_COL_NAME INTEGER PRIMARY KEY AUTOINCREMENT);")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TEXT_TABLE($TEXT_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,$TEXT_COL_FILE_PATH VARCHAR, $TEXT_COL_LANGUAGE VARCHAR, $TEXT_COL_NAME VARCHAR, $TEXT_COL_WORD_COUNT INTEGER);")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $WORD_TABLE($WORD_COL_TERM_ID INTEGER PRIMARY KEY AUTOINCREMENT, $WORD_COL_TERM VARCHAR, $WORD_COL_CLASS INTEGER, $WORD_COL_GLOSS VARCHAR, $WORD_COL_POS VARCHAR, $WORD_COL_EXAMPLE VARCHAR, $WORD_COL_NOTES VARCHAR, $WORD_COL_LANGUAGE VARCHAR);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $LANGUAGES_TABLE;")
        db?.execSQL("DROP TABLE IF EXISTS $TEXT_TABLE;")
        db?.execSQL("DROP TABLE IF EXISTS $WORD_TABLE;")
    }

    fun getWordById(id : Int) : Term? {
        val resSet : Cursor = readableDatabase.rawQuery("SELECT * from $WORD_TABLE WHERE $WORD_COL_TERM_ID = $id;", null)
        return if (resSet.count > 0) {
            parseWordsRes(resSet)[0]
        } else {
            Log.d("DBmanager", "nothing found for id = $id")
            resSet.close()
            null
        }
    }
    fun getWords(lang : String, term : String) : List<Term>? {
        val resSet :Cursor = readableDatabase.rawQuery("SELECT * from $WORD_TABLE WHERE $WORD_COL_LANGUAGE='$lang' AND $WORD_COL_TERM='$term';", null)
        return if (resSet.count > 0){
            Log.d("DBManager", "records found")
            parseWordsRes(resSet)
        } else {
            Log.d("DBManager", "no records found")
            resSet.close()
            null
        }

    }

    private fun parseWordsRes(resSet :Cursor) : List<Term> {
        val out = mutableListOf<Term>()

        with (resSet){
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(WORD_COL_TERM_ID))
                val term = getString(getColumnIndexOrThrow(WORD_COL_TERM))
                val gl = getString(getColumnIndexOrThrow(WORD_COL_GLOSS))
                val myclass = getInt(getColumnIndexOrThrow(WORD_COL_CLASS))
                val notes = getString(getColumnIndexOrThrow(WORD_COL_NOTES))
                val exs = getString(getColumnIndexOrThrow(WORD_COL_EXAMPLE))
                val pos = getString(getColumnIndexOrThrow(WORD_COL_POS))
                val lang = getString(getColumnIndexOrThrow(WORD_COL_LANGUAGE))
                out.add(Term(id, term, myclass, gl, pos, exs, notes, lang))
            }
        }
        resSet.close()
        return out
    }

    fun getWords(lang : String) : List<Term>?{
        val resSet : Cursor = readableDatabase.rawQuery("SELECT * from $WORD_TABLE WHERE $WORD_COL_LANGUAGE='$lang';", null)
        return parseWordsRes(resSet)
    }

    // $WORD_TABLE($WORD_COL_TERM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    // $WORD_COL_TERM VARCHAR,
    // $WORD_COL_CLASS INTEGER,
    // $WORD_COL_GLOSS VARCHAR,
    // $WORD_COL_POS VARCHAR,
    // $WORD_COL_EXAMPLE VARCHAR,
    // $WORD_COL_NOTES VARCHAR,
    // $WORD_COL_LANGUAGE VARCHAR);
    fun insertTerm(t : Term) {
        Log.d("DBmanager", "term id = ${t.id}")
        Log.d("DBmanager", "term class = ${t.myclass}")
        val query = """
            INSERT INTO $WORD_TABLE
            ($WORD_COL_TERM, $WORD_COL_CLASS, $WORD_COL_GLOSS,
             $WORD_COL_POS, $WORD_COL_EXAMPLE, $WORD_COL_NOTES, $WORD_COL_LANGUAGE)
            VALUES("${t.word}", ${t.myclass}, "${t.gloss}",
            "${t.pos}", "${t.examples}", "${t.notes}", "${t.language}");
            """
        writableDatabase.execSQL(query)
    }

    fun updateWord(t :Term) {
        Log.d("DBmanager", "Trying to update term with id = ${t.id}")
        var alreadyExists = getWordById(t.id)

        if (alreadyExists == null) {
            Log.d("DBmanager", "Term with id = ${t.id} did not exist. Inserting now.")
            insertTerm(t)
        } else {
            Log.d("DBmanager", "Running UPDATE for term with id = ${t.id}")
            var query = """UPDATE $WORD_TABLE
            SET $WORD_COL_TERM = "${t.word}", $WORD_COL_CLASS = ${t.myclass},
            $WORD_COL_GLOSS = "${t.gloss}", $WORD_COL_POS = "${t.pos}", $WORD_COL_EXAMPLE = "${t.examples}",
            $WORD_COL_NOTES = "${t.notes}", $WORD_COL_LANGUAGE = "${t.language}"
            WHERE $WORD_COL_TERM_ID = ${t.id};"""
            writableDatabase.execSQL(query)
            //TODO remove this
            var x = getWordById(t.id)

            if (x != null) {
                Log.d("DBmanager", "UPDATE done, term now has class of ${x.myclass}")
            } else {
                Log.d("DBmanager", "word not found after update")
            }
        }
    }
}




class Term(myid :Int, w : String, cl : Int, gl :String, mypos :String, exs :String, ns :String, lang : String) {
    val id = myid
    val word = w
    val myclass = cl
    val gloss = gl
    val pos = mypos
    val examples = exs
    val notes = ns
    val language = lang
}
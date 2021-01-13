package com.fionicholas.samplefirestore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_notes.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AddNotesActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AddNotesActivity::class.java))
        }
    }

    private val notesCollection =
        FirebaseFirestore.getInstance().collection("notes")

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        actionBar?.setDisplayHomeAsUpEnabled(true)
        if (supportActionBar != null) {
            supportActionBar?.title = getString(R.string.label_add_notes)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)

        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()

        btnAdd.setOnClickListener {
            addNotes()
        }
    }

    private fun addNotes() {
        val title = edtTitle.text.toString().trim()
        val message = edtMessage.text.toString().trim()
        val id = UUID.randomUUID().toString()
        val notes = Notes(id, title, message)

        uiScope.launch {
            addNotes(notes).collect { state ->
                when (state) {
                    is BaseResult.Loading -> {
                        dialog.show()
                    }

                    is BaseResult.Success -> {
                        dialog.dismiss()
                        MainActivity.start(this@AddNotesActivity)
                    }

                    is BaseResult.Failed -> {
                        dialog.dismiss()
                        Toast.makeText(this@AddNotesActivity, state.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private suspend fun addNotes(notes: Notes) = flow<BaseResult<DocumentReference>> {

        emit(BaseResult.loading())

        val notesRef = notesCollection.add(notes).await()

        emit(BaseResult.success(notesRef))

    }.catch {
        emit(BaseResult.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
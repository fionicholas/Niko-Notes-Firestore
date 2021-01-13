package com.fionicholas.samplefirestore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private val mainAdapter: MainAdapter by lazy {
        MainAdapter()
    }

    private val notesCollection =
        FirebaseFirestore.getInstance().collection("notes")

    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvNotes.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    RecyclerView.VERTICAL
                )
            )
            adapter = mainAdapter
        }

        uiScope.launch {
            getNotes().collect { state ->
                when (state) {
                    is BaseResult.Loading -> {
                        pbMain.visibility = View.VISIBLE
                    }

                    is BaseResult.Success -> {
                        pbMain.visibility = View.GONE
                        mainAdapter.setData(state.data)
                    }

                    is BaseResult.Failed -> {
                        pbMain.visibility = View.GONE
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fabAdd.setOnClickListener {
            AddNotesActivity.start(this)
        }
    }

    private suspend fun getNotes() = flow<BaseResult<List<Notes>>> {
        emit(BaseResult.loading())

        val snapshot = notesCollection.get().await()
        val notes = snapshot.toObjects(Notes::class.java)

        emit(BaseResult.success(notes))

    }.catch {
        emit(BaseResult.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

}

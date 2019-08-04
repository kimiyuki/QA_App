package shirai.kimiyuki.techacademy.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.solver.widgets.Snapshot
import android.util.Log
import android.view.View
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_fav.*
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class FavActivity() : AppCompatActivity() {

    lateinit var adapter:ExpandableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav)
        adapter = FavListAdapter(this)
        rvFaqs.setAdapter(adapter)
        Log.d("hello fav", "onCreate")
        rvFaqs.setOnGroupCollapseListener {groupPosition ->
           Toast.makeText(this, "hello:${groupPosition}",Toast.LENGTH_LONG)
        }
        rvFaqs.setOnGroupExpandListener { groupPosition ->
            Toast.makeText(this, "hello:${groupPosition}",Toast.LENGTH_LONG)
        }
        rvFaqs.setOnChildClickListener(object: ExpandableListView.OnChildClickListener{
            override fun onChildClick(
                parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long
            ): Boolean {
                val keys  = Qa_App.favGenreQuestions.keys.toIntArray()
                val grp = Qa_App.favGenreQuestions[keys[groupPosition]]
                val q = grp!![childPosition] as Map<String, String>
                val questionId = q["questionId"]
                val genre= q["genre"]
                sendQuestionDetail(genre=genre, questionId=questionId) { q ->
                    val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
                    intent.putExtra("question", q)
                    startActivity(intent)
                }
                return true
            }
        })
    }

    private fun sendQuestionDetail(genre:String?, questionId:String?, callback:(Question)->Unit){
        val ref = FirebaseDatabase.getInstance().reference
            .child(ContentsPATH).child(genre.toString()).orderByChild("questionId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(snapshot: DataSnapshot) {
                val m = snapshot.value as Map<String, Any>
                val mm = m[questionId] as Map<String, Any>
                val q = makeQuestionBySnap(snapshot.key!!, mm, genre!!.toInt())
                Log.d("hello q", "${q.title} : ${q.name}")
                callback(q!!)
            }
        })
    }
}

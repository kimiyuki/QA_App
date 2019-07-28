package shirai.kimiyuki.techacademy.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*
import shirai.kimiyuki.techacademy.qa_app.Model.Answer
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class QuestionDetailActivity : AppCompatActivity(){
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference

    private val mEventListener = object: ChildEventListener{
        override fun onCancelled(p0: DatabaseError) { }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) { }
        override fun onChildChanged(p0: DataSnapshot, p1: String?) { }
        override fun onChildRemoved(p0: DataSnapshot) { }

        override fun onChildAdded(snapshot: DataSnapshot, s: String?) {
            val map = snapshot.value as Map<String, String>
            val answerUid = snapshot.key ?: ""
            if (mQuestion.answers.any{ it.answerUid == answerUid}) return
            val answer = Answer( map["body"] ?: "", map["name"] ?: "", map["uid"] ?: "", answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

    }

    private val mfavoriteListener = object: ValueEventListener{
        override fun onCancelled(p0: DatabaseError) { }
        override fun onDataChange(datasnapshot: DataSnapshot) {
            Log.d("hello favorite", datasnapshot.value.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        mQuestion = intent.extras?.get("question") as Question
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listViewDetails.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fabQuestionDetail.setOnClickListener{ v ->
            val user = FirebaseAuth.getInstance().currentUser
            if(user == null){
                startActivity( Intent(applicationContext, LoginActivity::class.java) )
            }else{
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }
        val databaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = databaseReference.child(ContentsPATH).
            child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        mFavoriteRef = databaseReference.child(FavoritesPATH)
        mFavoriteRef.addValueEventListener(mfavoriteListener)

    }
}

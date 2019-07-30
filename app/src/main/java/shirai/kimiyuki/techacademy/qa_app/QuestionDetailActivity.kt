package shirai.kimiyuki.techacademy.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import shirai.kimiyuki.techacademy.qa_app.Model.Answer
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class QuestionDetailActivity : AppCompatActivity(){
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    private val databaseReference = FirebaseDatabase.getInstance().reference

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
        mAnswerRef = databaseReference.child(ContentsPATH).
            child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }

    private fun hasFav(uid:String, qid:String):Boolean{
        val userFavoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(uid)
        var ret =  true
        userFavoriteRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(s: DataSnapshot) {
                ret = s.children.any{
                    val m = it.value as Map<String, String>
                    qid == m["questionId"] } }
        })
        return ret
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null){
            buttonStar.visibility  = View.GONE
            return@onResume
        }

        buttonStar.visibility  = View.VISIBLE
        if(hasFav(user!!.uid, mQuestion.uid)){
            buttonStar.isChecked = true
        }
        mFavoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH)
        // お気に入りのリストを取得して、favボタンのり替え処理を反映
        buttonStar.setOnCheckedChangeListener{v, isChecked ->
            if (!isChecked){
               //Delete fav
                //mFavoriteRef.child(user.uid)
            }else {
                val data = HashMap<String, Any>()
                data["questionId"] = mQuestion.questionUid
                data["isFavorite"] = if (isChecked) 1 else 0
                data["genre"] = mQuestion.genre.toString()
                mFavoriteRef.child(user.uid).push().setValue(data)
            }
        }
    }
}

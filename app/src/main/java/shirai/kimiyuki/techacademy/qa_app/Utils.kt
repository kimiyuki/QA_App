package shirai.kimiyuki.techacademy.qa_app

import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import shirai.kimiyuki.techacademy.qa_app.Model.Answer
import shirai.kimiyuki.techacademy.qa_app.Model.Question

fun makeQuestionBySnap(k:String, m:Map<String,  Any>, mGenre:Int): Question {
    val title = m["title"] as String ?: ""
    val body = m["body"] as String ?: "" as String
    val name = m["name"] as String ?: "" as String
    val uid = m["uid"] as String ?: "" as String
    val imageString = (m["image"] ?: "") as String
    val bytes = if (imageString.isNotEmpty()) Base64.decode(imageString, Base64.DEFAULT) else byteArrayOf()

    val answerArrayList = ArrayList<Answer>()
    val answerMap = m["answers"] as Map<String, String>?
    if (answerMap != null) {
        for (key in answerMap.keys) {
            val temp = answerMap[key] as Map<String, String>
            val answerBody = temp["body"] ?: ""
            val answerName = temp["name"] ?: ""
            val answerUid = temp["uid"] ?: ""
            val answer = Answer(answerBody, answerName, answerUid, key)
            answerArrayList.add(answer)
        }
    }
    return Question(title=title, body=body, name = name, uid=uid, questionUid = k,
        genre=mGenre, bytes = bytes, answers = answerArrayList)
}

fun toggleFavData(mQuestion: Question){
    val user = FirebaseAuth.getInstance().currentUser
    if(user == null)return
    val userFavoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(user!!.uid)
    userFavoriteRef.addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onCancelled(p0: DatabaseError) { }
        override fun onDataChange(s: DataSnapshot) {
            val ret = s.children.
                filter(){
                    val m = it.value as Map<String, String>
                    mQuestion.questionUid == m["questionId"] }
            if(ret.isEmpty()){
                //create
                val data = HashMap<String, String>()
                data["genre"] = mQuestion.genre.toString()
                data["questionId"] = mQuestion.questionUid.toString()
                data["questionTitle"] = mQuestion.title.toString()
                userFavoriteRef.push().setValue(data).addOnCompleteListener {
                    if (Qa_App.favGenreQuestions[mQuestion.genre] == null) {
                        Qa_App.favGenreQuestions.put(mQuestion.genre, mutableListOf(data))
                        Log.d("hello fav", Qa_App.favGenreQuestions.toString())
                    } else {
                        Qa_App.favGenreQuestions[mQuestion.genre]?.add(data)
                        Log.d("hello add", Qa_App.favGenreQuestions[mQuestion.genre]?.size.toString())
                    }
                }
            }else{
                //remove
                userFavoriteRef.child(ret[0].key!!).removeValue().addOnCompleteListener {
                    Qa_App.favGenreQuestions[mQuestion.genre]?.removeIf {
                        it["questionId"] == mQuestion.questionUid }
                    Qa_App.favGenreQuestions = Qa_App.favGenreQuestions.filter{
                        it.value.size > 0
                    }.toMutableMap()
                    Log.d("hello remove fav", Qa_App.favGenreQuestions[mQuestion.genre]?.size.toString())
                }
            }
        }
    })
}

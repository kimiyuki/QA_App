package shirai.kimiyuki.techacademy.qa_app

import android.util.Log
import android.view.View
import android.widget.ToggleButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import shirai.kimiyuki.techacademy.qa_app.Model.Question

interface IFav {
    fun toggleFavData(q:Question)
    fun updateList(toggleButton:ToggleButton, q:Question)
}

class Fav(val databaseReference: DatabaseReference):IFav{

    override fun updateList(buttonStar: ToggleButton, mQuestion:Question){
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null)return
        val userFavoriteRef = databaseReference.child(FavoritesPATH).child(user!!.uid)
        userFavoriteRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(s: DataSnapshot) {
                val ret = s.children.any {
                    val m = it.value as Map<String, String>
                    mQuestion.questionUid == m["questionId"]
                }
                if(ret){ buttonStar.isChecked = true }
                buttonStar.setOnCheckedChangeListener{v, isChecked -> toggleFavData(mQuestion) }
            }
        })
        Log.d("hello Fav update", "aaa")
    }

    override fun toggleFavData(mQuestion: Question){
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null)return
        val userFavoriteRef = databaseReference.child(FavoritesPATH).child(user!!.uid)
        userFavoriteRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(s: DataSnapshot) {
                //has questionId?
                val ret = s.children.
                    filter(){
                        val m = it.value as Map<String, String>
                        mQuestion.questionUid == m["questionId"] }
                //if yes, add
                if(ret.isEmpty()){
                    //create
                    val data = HashMap<String, String>()
                    data["genre"] = mQuestion.genre.toString()
                    data["questionId"] = mQuestion.questionUid.toString()
                    data["questionTitle"] = mQuestion.title.toString()
                    userFavoriteRef.push().setValue(data)
                //no, remove
                }else{ userFavoriteRef.child(ret[0].key!!).removeValue() }
            }
        })
    }
}
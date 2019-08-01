package shirai.kimiyuki.techacademy.qa_app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class Qa_App: Application() {
    companion object {
        var favQuestions: MutableList<Map<String, String>>? = null
    }
    override fun onCreate() {
        super.onCreate()
        getFavs()
    }

    private fun getFavs(){
        val ref = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null) return
        ref.child(FavoritesPATH).child(user.uid).orderByKey().addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(snapshot: DataSnapshot) {
                favQuestions?.clear()
                snapshot.children.map{
                    val m = it.value as Map<String, String>
                    favQuestions?.add(m)
                }
            }
        })
    }
}
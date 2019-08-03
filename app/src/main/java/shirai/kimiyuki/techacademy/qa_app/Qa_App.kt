package shirai.kimiyuki.techacademy.qa_app

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Qa_App: Application() {
    companion object {
        var favQuestions: MutableList<Map<String, String>> = mutableListOf()
        var favGenreQuestions: MutableMap<Int, MutableList<Map<String,  String>>> = mutableMapOf()
    }
    override fun onCreate() {
        super.onCreate()
        getFavs()
    }

    private fun getFavs(){
        val ref = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        user ?: return
        ref.child(FavoritesPATH).child(user.uid).orderByKey().addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(snapshot: DataSnapshot) {
                favQuestions.clear()
                favGenreQuestions.clear()
                favGenreQuestions = snapshot.children
                    .map{ it.value as Map<String, String> }
                    .groupByTo(favGenreQuestions,  {
                        it["genre"]!!.toInt()
                    }).toMutableMap()
            }
        })
    }
}
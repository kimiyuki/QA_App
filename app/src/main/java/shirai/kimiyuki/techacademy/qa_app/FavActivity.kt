package shirai.kimiyuki.techacademy.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav)
        title= "お気に入り"
    }


    private fun <K,V>  getFavs(uid:String):List<Map<K,V>>?{
        val userFavoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(uid)
        var map:List<Map<K,V>>? = null
        userFavoriteRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(s: DataSnapshot) {
                s.children.map{
                    val m = it.value as Map<String, String>
                    Log.d("hello qq", """${"-LkknkCDonG1hesGg15w" == m["questionId"]}""")
                    m
                }
                //map  = s.children as List<Map<K, V>>
            }
        })
        return map
    }
}

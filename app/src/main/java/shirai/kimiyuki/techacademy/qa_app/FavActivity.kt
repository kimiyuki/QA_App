package shirai.kimiyuki.techacademy.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_fav.*

class FavActivity : AppCompatActivity() {

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
    }

}

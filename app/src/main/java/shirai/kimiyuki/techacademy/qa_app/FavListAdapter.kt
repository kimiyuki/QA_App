package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import android.widget.ToggleButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.item_fav_child.view.*
import kotlinx.android.synthetic.main.list_questions.view.*
import shirai.kimiyuki.techacademy.qa_app.Model.Question
import kotlin.coroutines.*

class FavListAdapter(val context: Context): BaseExpandableListAdapter() {
    val mLayoutInflater = LayoutInflater.from(context)
    val mRef = FirebaseDatabase.getInstance().reference
    private class ViewHolder(v:View){
        val title = v.titleTextView1
        val name = v.nameTextView1
        val resnum = v.resTextView1
        val imgView = v.imageView1
        val star = v.buttonStar2 as ToggleButton
    }

    fun getQfromFirebase(genre:Int, id:String, f:(Map<String, Any>)->Unit){
        lateinit var q: Question
        val s = mRef.child(ContentsPATH).child(genre.toString()).child(id).addListenerForSingleValueEvent(
            object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) { }
                override fun onDataChange(s: DataSnapshot) {
                   Log.d("aaa snap value", s.value.toString())
                    f(s.value as Map<String, Any>)
                   //q = s.value as Question
                } } )
    }

    override fun getGroup(groupPosition: Int): Any {
       val keys = Qa_App.favGenreQuestions.keys.toIntArray()
       return Qa_App.favGenreQuestions[keys[groupPosition]] as Any }
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean { return true }
    override fun hasStableIds(): Boolean {return false }
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView ?: mLayoutInflater.inflate(R.layout.item_fav_parent, null)
        val title = cv!!.findViewById<TextView>(R.id.textViewFavListParent)
        val grp = getGroup(groupPosition) as MutableList<Map<String, String>>
        if(grp.size > 0 ) { title.text = Qa_App.FavoriteMap[grp[0]["genre"]!!.toInt()] }
        return cv
    }
    override fun getChildrenCount(groupPosition: Int): Int { return (getGroup(groupPosition) as MutableList<Any>).size }
    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return (getGroup(groupPosition) as MutableList<Any>)!![childPosition] as Map<String, String> }
    override fun getGroupId(groupPosition: Int): Long { return groupPosition.toLong() }
    override fun getChildView( groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                               convertView: View?, parent: ViewGroup? ): View {
        var cv = convertView ?: mLayoutInflater.inflate(R.layout.list_questions, null)
        //var cv = convertView ?: mLayoutInflater.inflate(R.layout.item_fav_child, null)
        val fav = getChild(groupPosition, childPosition) as Map<String, String>
        val genre = Qa_App.favGenreQuestions.keys.toIntArray()[groupPosition]
        val holder = ViewHolder(cv)
        getQfromFirebase(genre, fav["questionId"]!!) { data ->
            val q = makeQuestionBySnap(fav["questionId"]!!, data, genre)
            Log.d("aaa hello", data.toString())
            holder.title.text = q.title
            holder.name.text = q.name
            holder.resnum.text = if (q.answers == null) "0" else q.answers.size?.toString()
            val bytes = q.imageBytes
            if(bytes.isNotEmpty()){
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).
                    copy(Bitmap.Config.ARGB_8888, true)
                holder.imgView.setImageBitmap(image)
            }
            holder.star.isChecked = true
            holder.star.setOnCheckedChangeListener{ _, _ -> toggleFavData(q) }
        }
        cv.tag = holder
        return cv
    }
    override fun getChildId(groupPosition: Int, childPosition: Int): Long { return childPosition.toLong() }
    override fun getGroupCount(): Int { return Qa_App.favGenreQuestions.keys.size }
}
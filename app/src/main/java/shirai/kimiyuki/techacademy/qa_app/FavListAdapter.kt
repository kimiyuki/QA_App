package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.item_fav_child.view.*

class FavListAdapter(val context: Context): BaseExpandableListAdapter() {
    val mLayoutInflater = LayoutInflater.from(context)
    override fun getGroup(groupPosition: Int): Any {
       val keys = Qa_App.favGenreQuestions.keys.toIntArray()
       return Qa_App.favGenreQuestions[keys[groupPosition]] as Any
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean { return true }

    override fun hasStableIds(): Boolean {return false }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView ?: mLayoutInflater.inflate(R.layout.item_fav_parent, null)
        val title = cv!!.findViewById<TextView>(R.id.textViewFavListParent)
        val grp = getGroup(groupPosition) as MutableList<Map<String, String>>
        title.text = grp[0]["genre"]
        return cv
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        //groupPosition == mGenre?
        return Qa_App.favGenreQuestions[groupPosition+1]!!.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return Qa_App.favGenreQuestions[groupPosition+1]!![childPosition] as Map<String, String>
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView( groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                               convertView: View?, parent: ViewGroup? ): View {
        var cv = convertView ?: mLayoutInflater.inflate(R.layout.item_fav_child, null)
        val fav = getChild(groupPosition, childPosition) as Map<String, String>
        Log.d("hello fav child", fav.toString())
        cv.textViewFavChildTitle.text = fav["questionTitle"]
        cv.textViewFavChildBody.text = fav["questionTitle"]
        return cv
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return Qa_App.favGenreQuestions.keys.size
    }
}
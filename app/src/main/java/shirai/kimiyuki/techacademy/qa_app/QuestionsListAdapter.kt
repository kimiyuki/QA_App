package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ToggleButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_question_detail.view.*
import kotlinx.android.synthetic.main.list_question_detail.view.*
import kotlinx.android.synthetic.main.list_questions.view.*
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class QuestionsListAdapter(context: Context):BaseAdapter() {

    private var mLayoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mQuestionArrayList = ArrayList<Question>()


    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        Log.d("hello question", questionArrayList.size.toString())
        mQuestionArrayList = questionArrayList
    }

    private class ViewHolder(v:View){
        val title = v.titleTextView1
        val name = v.nameTextView1
        val resnum = v.resTextView1
        val imgView = v.imageView1
        val star = v.buttonStar2 as ToggleButton
    }

    private fun isFav(mQ:Question):Boolean{
        return Qa_App.favGenreQuestions[mQ.genre]!!.any{
            mQ.questionUid == it["questionId"]
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("hello getView", position.toString())
        var v= convertView ?: mLayoutInflater.inflate(R.layout.list_questions, parent, false)
        val mQ = getItem(position) as Question
        val holder = ViewHolder(v)
        holder.title.text = mQ.title
        holder.name.text = mQ.name
        holder.resnum.text = mQ.answers.size.toString()
        val bytes = mQ.imageBytes
        if(bytes.isNotEmpty()){
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).
                copy(Bitmap.Config.ARGB_8888, true)
            holder.imgView.setImageBitmap(image)
        }
        holder.star.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("hello toggle", isChecked.toString())
        }
        val user = FirebaseAuth.getInstance().currentUser
        if(user==null){
            holder.star.visibility = View.GONE
        }else {
            holder.star.visibility = View.VISIBLE
            if(isFav(mQ)) holder.star.isChecked = true
        }
        v.tag = holder
        return v
    }


    override fun getItem(position: Int): Any {
        return mQuestionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
            return position.toLong()
    }

    override fun getCount(): Int {
        return mQuestionArrayList.size
    }
}
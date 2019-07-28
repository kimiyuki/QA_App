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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("hello getView", position.toString())
        var v= convertView ?: mLayoutInflater.inflate(R.layout.list_questions, parent, false)
        val mQ = getItem(position) as Question
        v.titleTextView1.setText(mQ.title)
        v.nameTextView1.setText(mQ.name)
        v.resTextView1.setText(mQ.answers.size.toString())
        val bytes = mQ.imageBytes
        if(bytes.isNotEmpty()){
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).
                copy(Bitmap.Config.ARGB_8888, true)
            v.imageView1.setImageBitmap(image)
        }
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
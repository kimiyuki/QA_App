package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.list_questions.view.*
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class QuestionsListAdapter(context: Context):BaseAdapter() {

    private var mLayoutInflater: LayoutInflater
    private var mQuestionArrayList = ArrayList<Question>()

    init{
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        Log.d("hello question", questionArrayList.size.toString())
        mQuestionArrayList = questionArrayList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("hello getView", position.toString())
        var v= convertView ?: mLayoutInflater.inflate(R.layout.list_questions, parent, false)
        v.titleTextView1.setText(mQuestionArrayList[position].title)
        v.nameTextView1.setText(mQuestionArrayList[position].name)
        v.resTextView1.setText(mQuestionArrayList[position].answers.size.toString())
        val bytes = mQuestionArrayList[position].imageBytes
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
package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.list_answer.view.*
import kotlinx.android.synthetic.main.list_answer.view.bodyTextView
import kotlinx.android.synthetic.main.list_answer.view.nameTextView
import kotlinx.android.synthetic.main.list_question_detail.view.*
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {

    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWEWR = 1
    }
    private var mLayoutInflater: LayoutInflater? = null
    init { mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater }

    override fun getItem(position: Int): Any {return mQuestion }
    override fun getItemId(position: Int): Long {return 0}
    override fun getCount(): Int { return 1 + mQuestion.answers.size}

    override fun getItemViewType(position: Int): Int { return if (position == 0) TYPE_QUESTION else TYPE_ANSWEWR}

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val lv = if(getItemViewType(position)== TYPE_QUESTION) R.layout.list_question_detail else R.layout.list_answer
        var cv = convertView ?: mLayoutInflater!!.inflate(lv, parent, false)

        if(getItemViewType(position)== TYPE_QUESTION){
            cv.bodyTextView.text = mQuestion.body
            cv.nameTextView.text =  mQuestion.name
            val bytes = mQuestion.imageBytes
            if(bytes.isNotEmpty()){
                cv.imageView2.setImageBitmap(
                    BitmapFactory.decodeByteArray(bytes,0,bytes.size).copy(Bitmap.Config.ARGB_8888,true)
                ) }
        }else if(getItemViewType(position) == TYPE_ANSWEWR){
            val answer = mQuestion.answers[position - 1]
            cv.bodyTextView.text = answer.body
            cv.nameTextView.text = answer.name
        }else{ throw Exception("hello exception") }

        return cv!!
    }

}

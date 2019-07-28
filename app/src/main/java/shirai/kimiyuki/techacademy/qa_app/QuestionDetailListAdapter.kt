package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.list_answer.view.*
import kotlinx.android.synthetic.main.list_answer.view.bodyTextView
import kotlinx.android.synthetic.main.list_answer.view.nameTextView
import kotlinx.android.synthetic.main.list_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.view.*
import kotlinx.android.synthetic.main.list_question_detail.view.buttonStar
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

    private fun isFavorite(userId:String?):Boolean{
        //TODO
//        if(userId == null) return false
//        FirebaseDatabase.getInstance().reference.child(FavoritePATH)
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val lv = if(getItemViewType(position)== TYPE_QUESTION) R.layout.list_question_detail else R.layout.list_answer
        var cv = convertView ?: mLayoutInflater!!.inflate(lv, parent, false)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val questionId = mQuestion.questionUid

        if(getItemViewType(position)== TYPE_QUESTION){
            cv.bodyTextView.text = mQuestion.body
            cv.nameTextView.text =  mQuestion.name
            //cv.buttonStar.setBackgroundResource(R.drawable.btn_pressed)

            cv.buttonStar.setOnClickListener {v ->
                if(v !is ImageButton) return@setOnClickListener
                val databaseReference = FirebaseDatabase.getInstance().reference
                val favoriteRef = databaseReference.child(FavoritesPATH)
                val data = HashMap<String, Any>()
                data["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
                data["questionId"] = mQuestion.questionUid
                data["isFavorite"] = if (isFavorite(data["userId"] as String))  1 else 0
                favoriteRef.push().setValue(data)
            }
            cv.buttonStar.setBackgroundResource(
                if(isFavorite(userId)) R.drawable.btn else R.drawable.btn_pressed)

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

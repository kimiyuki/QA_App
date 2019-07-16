package shirai.kimiyuki.techacademy.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.renderscript.Sampler
import android.support.design.widget.Snackbar
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()

        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合 ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else {
                // 失敗した場合  エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        mLoginListener = OnCompleteListener { task ->
           if(task.isSuccessful){
               val user =  mAuth.currentUser
               val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)

               if(mIsCreateAccount){
                   val data = HashMap<String, String>()
                   data["name"] = nameText.text.toString()
                   userRef.setValue(data)
                   saveName(nameText.text.toString())
               }else{
                   userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                       override fun onCancelled(p0: DatabaseError) {
                           TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                       }

                       override fun onDataChange(snapshot: DataSnapshot) {
                           val data = snapshot.value as Map<*, *>?
                           saveName(data!!["name"]  as String)
                       }
                   })
                   progressBar.visibility = View.GONE
                   finish()
               }
           }else{
               val view = findViewById<View>(android.R.id.content)
               Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG)
               progressBar.visibility = View.GONE
           }
        }

        title = ""

        createButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        loginButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

    }

    private fun createAccount(email:String, password:String){
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)

    }

    private fun login(email:String, password: String){
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    private fun saveName(name:String){
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }

}

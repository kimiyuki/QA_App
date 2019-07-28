package shirai.kimiyuki.techacademy.qa_app

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*
import java.lang.ref.PhantomReference

class SettingActivity : AppCompatActivity() {

    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        val nameText = nameText
        nameText.setText(name)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        title = "設定"

        changeButton.setOnClickListener{ v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val user = FirebaseAuth.getInstance().currentUser

            if(user == null){
                Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
            }else{
                val name = nameText.text.toString()
                val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name
                userRef.setValue(data)

                val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp.edit()
                editor.putString(NameKEY,name)
                editor.commit()
                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener{v ->
            if(logoutButton.text == "ログイン"){
                Log.d("hello login", "a")
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }else {
                FirebaseAuth.getInstance().signOut()
                nameText.setText("")
                logoutButton.setText("ログイン")
                Snackbar.make(v, "ログアウトしました。", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}

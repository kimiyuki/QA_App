package shirai.kimiyuki.techacademy.qa_app

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import kotlinx.android.synthetic.main.activity_question_send.*

import java.io.ByteArrayOutputStream
import java.util.HashMap

class QuestionSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var mGenre: Int = 0
    private var mPictureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_send)

        val extras = intent.extras
        mGenre = extras.getInt("genre")

        title = "質問作成"

        sendButton.setOnClickListener(this)
        imageView.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        if(v === imageView){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    showChooser()
                }else{
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                    return
                }
            }else{
                showChooser()
            }

        }else if(v === sendButton) {
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val databaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = databaseReference.child(ContentsPATH).child(mGenre.toString())

            val data = HashMap<String, String>()

            data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

            val title = titleText.text.toString()
            val body = bodyText.text.toString()

            if(title.isEmpty()){
                Snackbar.make(v, "タイトルを入力してください", Snackbar.LENGTH_LONG).show()
                return
            }

            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")

            data["title"] = title
            data["body"] = body
            data["name"] = name

            val drawable = imageView.drawable as? BitmapDrawable

            if(drawable != null){
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["image"] = bitmapString
            }
            genreRef.push().setValue(data, this)
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onComplete(databaseError: DatabaseError?, databaseRef: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }

     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
         when(requestCode){
             PERMISSIONS_REQUEST_CODE -> {
                 if(grantResults[0] == PackageManager.PERMISSION_GRANTED){ showChooser() }
                 return } } }

    private fun showChooser(){
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)


        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")
        cameraIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

}

package shirai.kimiyuki.techacademy.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity;
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import shirai.kimiyuki.techacademy.qa_app.Model.Answer
import shirai.kimiyuki.techacademy.qa_app.Model.Question

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mGenre = 0
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter


    //for questions list at a genre
    private var mGenreRef: DatabaseReference? = null
    private val mEventListener = object: ChildEventListener {
        override fun onCancelled(p0: DatabaseError) { }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) { }
        override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
            val values  = dataSnapshot.value as Map<String, String>
            mQuestionArrayList.filter{ dataSnapshot.key.equals(it.questionUid)}
                .forEach{
                    it.answers.clear()
                    val answerMap = values["answers"] as Map<String, String>?
                    if(answerMap == null) return
                    for(key in answerMap.keys){
                        val temp = answerMap[key] as Map<String, String>
                        it.answers.add( Answer(
                            temp["body"] ?: "", temp["name"] ?: "", temp["uid"] ?: "", key )) } }
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, position: String?) {
            Log.d("hello onChildAdd", "onChildAdd")
            val question = questionFactory(dataSnapshot)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }
        override fun onChildRemoved(p0: DataSnapshot) {}
    }

    private fun questionFactory(dataSnapshot: DataSnapshot): Question {
        val map = dataSnapshot.value as Map<String, String>
        val title = map["title"] ?: ""
        val body = map["body"] ?: ""
        val name = map["name"] ?: ""
        val uid = map["uid"] ?: ""
        val imageString = map["image"] ?: ""
        val bytes = if (imageString.isNotEmpty()) Base64.decode(imageString, Base64.DEFAULT) else byteArrayOf()

        val answerArrayList = ArrayList<Answer>()
        val answerMap = map["answers"] as Map<String, String>?
        if (answerMap != null) {
            for (key in answerMap.keys) {
                val temp = answerMap[key] as Map<String, String>
                val answerBody = temp["body"] ?: ""
                val answerName = temp["name"] ?: ""
                val answerUid = temp["uid"] ?: ""
                val answer = Answer(answerBody, answerName, answerUid, key)
                answerArrayList.add(answer)
            }
        }
        return Question(title, body, name, uid, dataSnapshot.key ?: "", mGenre, bytes, answerArrayList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {view ->
            if(mGenre == 0){
                Snackbar.make(view,"ジャンルを設定してください",Snackbar.LENGTH_LONG )
            }
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("hello_qa", user.toString())
            if(user == null){
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        Log.d("hello", "${toggle is DrawerLayout.DrawerListener}")
        Log.d("hello", "${toggle::class}")
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener{parent, view, position, id ->
            val intent  = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    private fun getFavs(){
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null) return
        val userFavoriteRef = mDatabaseReference.child(FavoritesPATH).child(user!!.uid)
        userFavoriteRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(s: DataSnapshot) {
                mQuestionArrayList.clear()
                s.children.forEach{
                    val q = it.value as Map<String, String>
                    val qid = q["questionId"]
                    val genre = q["genre"]
                    Log.d("hello qid", qid)
                    mDatabaseReference.child(ContentsPATH).child(genre!!).orderByChild("questionId")
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) { }
                            override fun onDataChange(s: DataSnapshot) {
                                s.children.filter {
                                    Log.d("hello key", it.key)
                                    it.key == qid
                                }.forEach{
                                    Log.d("hello processed", it.key)
                                    val q = questionFactory(it)
                                    Log.d("hello q", q.toString())
                                    mQuestionArrayList.add(q)
                                }
                                mAdapter.setQuestionArrayList(mQuestionArrayList)
                                mAdapter.notifyDataSetChanged()
                            }
                        })
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //show default item in the Menu
        if(mGenre == 0){ onNavigationItemSelected(nav_view.menu.getItem(0)) }
        val user =FirebaseAuth.getInstance().currentUser
        title = user?.uid ?: "no user"

        //login or not
        if(user == null){
            nav_view.menu.removeItem(R.id.nav_fav)
        }else {
            Log.d("hello nav", "he")
            //getFavs<String, String>(user.uid)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if(id == R.id.action_settings){
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
       return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var isFav:Boolean = false
        when(item.itemId){
            R.id.nav_hobby -> { toolbar.title = "趣味"; mGenre = 1}
            R.id.nav_life -> { toolbar.title = "生活"; mGenre = 2}
            R.id.nav_health -> { toolbar.title = "健康"; mGenre = 3}
            R.id.nav_compter -> { toolbar.title = "コンピューター"; mGenre = 4}
            R.id.nav_fav -> { toolbar.title = "お気に入り"; isFav = true}
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        if (!isFav) {
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)
        } else {
            Log.d("hello qqq", "fav")
            getFavs()
        }

        return true
    }

}

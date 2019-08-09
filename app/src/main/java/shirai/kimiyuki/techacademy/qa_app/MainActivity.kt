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
            val m = dataSnapshot.value as Map<String, String>
            val question = makeQuestionBySnap(dataSnapshot.key!!, m, mGenre)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }
        override fun onChildRemoved(p0: DataSnapshot) {}
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
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener{parent, view, position, id ->
            Log.d("hello p", position.toString())
            val intent  = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val user =FirebaseAuth.getInstance().currentUser
        nav_view.menu.findItem(R.id.nav_fav).setVisible(user != null)
        if(mGenre == 0){
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_hobby))
        }else{
            onNavigationItemSelected(nav_view.menu.getItem(mGenre-1))
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
        when (item.itemId) {
            R.id.nav_hobby -> { toolbar.title = Qa_App.FavoriteMap[1]; mGenre = 1 }
            R.id.nav_life -> { toolbar.title = Qa_App.FavoriteMap[2]; mGenre = 2 }
            R.id.nav_health -> { toolbar.title = Qa_App.FavoriteMap[3]; mGenre = 3 }
            R.id.nav_compter -> { toolbar.title = Qa_App.FavoriteMap[4]; mGenre = 4 }
            R.id.nav_fav -> {
                val intent = Intent(this, FavActivity::class.java)
                startActivity(intent)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        if (mGenreRef != null) { mGenreRef!!.removeEventListener(mEventListener) }
        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
        return true
    }

}

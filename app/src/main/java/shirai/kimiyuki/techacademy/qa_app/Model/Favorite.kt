package shirai.kimiyuki.techacademy.qa_app.Model

import java.io.Serializable

class Favorite(val uid:String, val user_uid:String, val questions: ArrayList<Question>): Serializable {
}
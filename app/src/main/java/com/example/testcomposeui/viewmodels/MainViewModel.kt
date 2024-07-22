package com.example.testcomposeui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.testcomposeui.MyApplication
import com.example.testcomposeui.db.CampingSite
import com.example.testcomposeui.db.User
import com.example.testcomposeui.db.UserDao
import com.example.testcomposeui.db.UserDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

//MVVM 모델 과 컴포스를 적용
class MainViewModel : BaseViewModel() {

    val TAG = MainViewModel::class.java.simpleName

    private val userDao: UserDao = UserDatabase.getDatabase(MyApplication.context).userDao()
    val user: LiveData<User?> = userDao.getUser().asLiveData()

    fun getLoginUser(onComplete: (User?) -> Unit) {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                Log.d(TAG, "getLoginUser() = $user")
                onComplete(user)
            }
        }
    }

    fun checkUser(user: User, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            //db에 저장된 사용자 정보를 이용하여 로그인 신청.
            Log.d(TAG, "checkUser() = $user")
            emailLogin(
                email = user.email,
                password = user.password,
                onComplete = onComplete
            )
        }
    }


    fun emailLogin(
        email: String,
        password: String,
        saveUserData: Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {
        // 파이어베이스 이메일 로그인.
        Log.d(TAG, "emailLogin() = $email, $password")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    firebaseAuthTokenLogin(email = email, password = password, saveUserData = saveUserData,
                        onComplete)
                }
            }.addOnFailureListener { exception ->
                if (exception is FirebaseAuthException) {
                    val errorCode = exception.errorCode
                    Log.e(TAG, "로그인 실패: ${exception.message}")
                    when (errorCode) {
                        "ERROR_INVALID_EMAIL" -> onComplete(false, "잘못된 이메일 형식입니다.")
                        "ERROR_WRONG_PASSWORD" -> onComplete(false, "비밀번호가 틀렸습니다.")
                        "ERROR_USER_NOT_FOUND" -> onComplete(false, "사용자를 찾을 수 없습니다.")
                        "ERROR_USER_DISABLED" -> onComplete(false, "계정이 비활성화 되었습니다.")
                        "ERROR_INVALID_CREDENTIAL" -> onComplete(false, "잘못된 자격 증명입니다.")
                        else -> onComplete(false, "로그인 실패: ${exception.message}")
                    }
                } else {
                    onComplete(false, "로그인 실패: ${exception.message}")
                }
            }
    }

    fun emailRegisterUser(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        // 파이어베이스 이메일 회원가입.
        Log.d(TAG, "emailRegisterUser() = $email, $password")
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 이메일 등록 성공 - 새 사용자 처리
                    firebaseAuthTokenLogin(email, password, true, onComplete)
                } else {
                    //가입 실패.
                    try {
                        throw task.exception!!
                    } catch (existEmail: FirebaseAuthUserCollisionException) {
                        // 이메일이 이미 사용 중임을 사용자에게 알림
                        onComplete(false, "이 이메일 주소는 이미 사용 중입니다.")
                    } catch (e: Exception) {
                        // 기타 잠재적인 예외 처리
                        onComplete(false, "등록 실패")
                    }
                }
            }
    }

    private fun firebaseAuthTokenLogin(
        email: String, password: String, saveUserData: Boolean,
        onComplete: (Boolean, String) -> Unit
    ) {
        // 파이어베이스 인증
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.getIdToken(true)
            ?.addOnCompleteListener { idTokenTask ->
                if (idTokenTask.isSuccessful) {
                    val idToken = idTokenTask.result?.token
//                    Log.d(TAG, "registerUser() idTokenTask.isSuccessful = $idToken")
                    auth.currentUser?.let { firebaseUser ->
                        viewModelScope.launch {
                            // 사용자 정보를 데이터베이스에 저장
                            if (saveUserData) userDaoInsert(firebaseUser, email, password)
                            onComplete(true, "계정 생성에 성공 하였습니다.")
                        }
                    }

                } else {
                    onComplete(false, "계정 생성에 실패 하였습니다.")
                }
            }
    }

    suspend fun userDaoInsert(firebaseUser: FirebaseUser, email: String, password: String) {
        // 사용자 정보를 데이터베이스에 저장
        val newUser = User(
            uid = firebaseUser.uid,
            email = email,
            password = password,
            username = firebaseUser.displayName ?: ""
        )
        userDao.insertUser(newUser)
    }

    fun firebaseAuthWithGoogle(idToken: String, onComplete: (Boolean, String) -> Unit) {
        // 파이어베이스 구글 메일 인증
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 성공 처리
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    // 실패 처리
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private val _my_camping_list = MutableLiveData<List<CampingSite>>()
    val my_camping_list: LiveData<List<CampingSite>> = _my_camping_list

    fun checkUserExists(userId: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(true) // 기존 회원
                } else {
                    onResult(false) // 최초 가입
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document: ", exception)
                onResult(false) // 오류 발생 시 최초 가입으로 간주
            }
    }


    fun getAllCampingSites(onComplete: (Boolean) -> Unit) {
        //파이어스토어 데이터베이스에 저장된 캠핑장 정보 가져오기.
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            Log.d(TAG, "getAllCampingSites() = $uid")
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .collection("my_camping_list")
                .get()
                .addOnSuccessListener { result ->
                    Log.d(TAG, "getAllCampingSites() addOnSuccessListener = $result")
                    val campingSites = mutableListOf<CampingSite>()
                    for (document in result) {
                        val campingSite = document.toObject(CampingSite::class.java)
//                        Log.d(TAG, "getAllCampingSites() campingSite = $campingSite")
                        campingSites.add(campingSite)
                    }
                    _my_camping_list.value = campingSites
                    onComplete(true)
                }
                .addOnFailureListener { e ->
//                onComplete(emptyList())
                    Log.e(TAG, "Error getting documents: ", e)
                    _my_camping_list.value = emptyList()
                    onComplete(false)
                }
        }
    }

    fun deleteCampingSite(id: String, onComplete: (Boolean) -> Unit) {
        //파이어스토어 데이터베이스에 저장된 캠핑장 정보 삭제.
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            Log.d(TAG, "deleteCampingSite() = $uid")
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .collection("my_camping_list").document(id)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    // 삭제 성공 처리
                    _my_camping_list.value = deleteCampingList(id)
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    println("Error deleting document: $e")
                    // 삭제 실패 처리
                    onComplete(false)
                }
        }
    }

    fun deleteCampingList(id: String): MutableList<CampingSite> {
        val campingSites: MutableList<CampingSite> =
            _my_camping_list.value as MutableList<CampingSite>
        campingSites.removeIf { it.id == id }
        return campingSites

    }

}
package com.example.testcomposeui.viewmodels

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.testcomposeui.api.RetrofitInstance
import com.example.testcomposeui.data.CampingSite
import com.example.testcomposeui.data.User
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//MVVM 모델 과 컴포스를 적용
class MainViewModel: BaseViewModel(){

    val TAG = MainViewModel::class.java.simpleName

    val compositeDisposable = CompositeDisposable()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

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
        val db = FirebaseFirestore.getInstance()
        db.collection("my_camping_list")
            .get()
            .addOnSuccessListener { result ->
                val campingSites = mutableListOf<CampingSite>()
                for (document in result) {
                    val campingSite = document.toObject(CampingSite::class.java)
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

    fun deleteCampingSite(id: String, onComplete: (Boolean) -> Unit) {
        //파이어스토어 데이터베이스에 저장된 캠핑장 정보 삭제.
        val db = FirebaseFirestore.getInstance()
        db.collection("my_camping_list").document(id)
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

    fun deleteCampingList(id: String): MutableList<CampingSite>  {
        val campingSites: MutableList<CampingSite> = _my_camping_list.value as MutableList<CampingSite>
        campingSites.removeIf { it.id == id }
        return campingSites

    }

    fun fetchUsers() {
        val disposable = RetrofitInstance.api.getUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { userList ->
                    for (user in userList) {
                        Log.d(TAG, user.toString())
                    }
                    _users.value = userList
                },
                { error -> error.printStackTrace() }
            )
        compositeDisposable.add(disposable)
    }

//    private fun fetchUsers() {
//        Log.d(TAG, "fetchUsers()")
//        // 코루틴을 사용하여 API 호출
//        viewModelScope.launch {
//            try {
//                val response = RetrofitInstance.api.getUsers()
//                if (response.isSuccessful) {
//                    // API 호출 성공
//                    val userList = response.body() ?: emptyList()
//                    for (user in userList) {
//                        Log.d(TAG, user.toString())
//                    }
//                    _users.value = userList
//                } else {
//                    // Handle HTTP error
//                    Log.e(TAG, "API 호출 실패: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                // Handle exceptions
//                Log.e(TAG, "API 호출 실패: ${e.message}")
//            }
//        }
//    }


    private val _navigateToSecondActivity = MutableStateFlow(false)
    val navigateToSecondActivity: StateFlow<Boolean> = _navigateToSecondActivity

    private val _text = MutableStateFlow("Hello, World!")
    val text: StateFlow<String> = _text

    private val _checked = MutableStateFlow(false)
    val checked: StateFlow<Boolean> = _checked

    private val _sliderPosition = MutableStateFlow(0.5f)
    val sliderPosition: StateFlow<Float> = _sliderPosition

    fun onTextChanged(newText: String) {
        _text.value = newText
    }

    fun onCheckedChanged(newChecked: Boolean) {
        _checked.value = newChecked
    }

    fun onSliderPositionChanged(newPosition: Float) {
        _sliderPosition.value = newPosition
    }

    fun onButtonClick() {
        _navigateToSecondActivity.value = true
    }

    fun onNavigatedToSecondActivity() {
        _navigateToSecondActivity.value = false
    }



}
package com.example.testcomposeui.data

import androidx.compose.runtime.Immutable

@Immutable  //절대적으로 변경되지 않음을 정의.
data class User(
    val id: Int,
    val name: String?,
    val username: String?,
    val email: String?,
    val address: Address?,
    val phone: String?,
    val website: String?,
    val company: Company?,
    var imgUrl: String = "https://randomuser.me/api/portraits/women/11.jpg"
) {
    override fun toString(): String {
        return "User(id=$id, name=$name, username=$username, email=$email, address=$address, phone=$phone, website=$website, company=$company, imgUrl='$imgUrl')"
    }

}

data class Address(
    var street: String?,
    var suite: String?,
    var city: String?,
    var zipcode: String?,
    var geo: Geo
)

data class Geo(
    var lat: String?,
    var lng: String?
)

data class Company(
    var name: String?,
    var catchPhrase: String?,
    var bs: String?
)

package com.example.testcomposeui.data

data class User(
    var id: Int,
    var name: String?,
    var username: String?,
    var email: String?,
    var address: Address?,
    var phone: String?,
    var website: String?,
    var company: Company?
) {
    override fun toString(): String {
        return "User(id=$id, name=$name, username=$username, email=$email, address=$address, phone=$phone, website=$website, company=$company)"
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

package com.example.testcomposeui.data

import com.example.testcomposeui.db.CampingSite

data class DifferenceCampingSiteResult(
    val remoteOnly: List<CampingSite>,
    val localOnly: List<CampingSite>
)

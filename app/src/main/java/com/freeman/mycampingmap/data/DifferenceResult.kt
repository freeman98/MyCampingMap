package com.freeman.mycampingmap.data

import com.freeman.mycampingmap.db.CampingSite

data class DifferenceCampingSiteResult(
    val remoteOnly: List<CampingSite>,
    val localOnly: List<CampingSite>
)

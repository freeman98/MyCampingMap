package com.freeman.mycampingmap.data

import android.content.Context
import android.widget.Toast
import com.freeman.mycampingmap.auth.FirebaseManager
import com.freeman.mycampingmap.db.CampingSite
import com.freeman.mycampingmap.db.CampingSiteRepository
import com.freeman.mycampingmap.utils.MyLog
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class CampingDataUtil @Inject constructor(
    val context: Context,
    private var firbaseManager: FirebaseManager,
) {

    val TAG: String = this::class.java.simpleName
//    @Inject lateinit var firbaseManager: FirebaseManager

    fun createCampingSiteData(place: Place): CampingSite {
        // 캠핑장 정보 객체 생성
        return CampingSite(
            id = place.id?.toString() ?: "",
            name = place.name ?: "",
            address = place.address ?: "",
            phoneNumber = place.phoneNumber ?: "",
            location = place.latLng?.toString() ?: "",
            rating = place.rating?.toString() ?: "",
            reviews = place.reviews?.toString() ?: "",
            websiteUri = place.websiteUri?.toString() ?: ""
        )
    }

    fun syncCampingSites(
//        firbaseManager: FirebaseManager,
        localSites: List<CampingSite>,
        remoteSites: List<CampingSite>,
        campingSiteRepository: CampingSiteRepository,
    ): List<CampingSite> {
        // 캠핑장 데이터 리스트 동기화
        MyLog.d(TAG, "syncCampingSites()")
        val mergedSites = getUniqueUnion(localSites, remoteSites)
        findDifferences(localSites, remoteSites) { localOnly, remoteOnly ->
            // 로컬 DB과 파이어베이스에 없는 캠핑장 정보
            MyLog.d(TAG, "syncCampingSites() localOnly.size: ${localOnly.size}")
            MyLog.d(TAG, "syncCampingSites() remoteOnly.size: ${remoteOnly.size}")

            // 파이어베이스에만 있는 캠핑장 정보는 db에 저장.
            if (remoteOnly.size > 0) {
                MyLog.d(TAG, "syncCampingSites() campingSiteRepository.insertAll")
                campingSiteRepository.insertAll(remoteOnly)
            }
            // db에만 있는 캠핑장 정보는 파이어베이스에 저장.
            if (localOnly.size > 0) {
                firbaseManager.addFirebaseCampingSites(localOnly) {
                    MyLog.d(TAG, "syncCampingSites() addFirebaseCampingSites()")
                    if (!it) Toast.makeText(context, "캠핑장 정보 저장 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }   //findDifferences

        return mergedSites
    }

    private fun getUniqueUnion(
        // 캠핑장 리스트가 다를 경우 동기화 작업
        localSites: List<CampingSite>,
        remoteSites: List<CampingSite>,
    ): List<CampingSite> {
        val allSites = (localSites + remoteSites)
        //
        return allSites.distinctBy { it.id }
    }

    private fun findDifferences(
        //캠핑장 리스트가 다를 경우 동기화 작업
        localCampingSites: List<CampingSite>,
        remoteCampingSites: List<CampingSite>,
        differenceCampingSiteResult: (List<CampingSite>, List<CampingSite>) -> Unit,
    ) {
        // 원격 DB에 없는 로컬 캠핑장 정보
        val localOnly = localCampingSites.filter { localSite ->
            remoteCampingSites.none { remoteSite -> remoteSite.id == localSite.id }
        }
        // 로컬 DB에 없는 원격 캠핑장 정보
        val remoteOnly = remoteCampingSites.filter { remoteSite ->
            localCampingSites.none { localSite -> localSite.id == remoteSite.id }
        }
        differenceCampingSiteResult(localOnly, remoteOnly)
    }
}
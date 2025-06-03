package edu.stanford.bdh.engagehf.modules.education

import edu.stanford.bdh.engagehf.modules.education.videos.Video
import kotlinx.serialization.Serializable

@Serializable
sealed class EducationRoutes {

    @Serializable
    data class VideoDetail(val video: Video) : EducationRoutes()
}

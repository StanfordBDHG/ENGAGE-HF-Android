package edu.stanford.bdh.engagehf.modules.education

import edu.stanford.bdh.engagehf.modules.education.videos.Video
import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent

sealed class EducationNavigationEvent : NavigationEvent {
    data class VideoSectionClicked(val video: Video) :
        EducationNavigationEvent()
}

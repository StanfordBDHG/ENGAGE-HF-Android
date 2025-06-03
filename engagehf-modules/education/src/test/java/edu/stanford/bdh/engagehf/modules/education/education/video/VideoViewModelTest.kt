package edu.stanford.bdh.engagehf.modules.education.education.video

import androidx.lifecycle.SavedStateHandle
import edu.stanford.bdh.engagehf.modules.education.video.Action
import edu.stanford.bdh.engagehf.modules.education.video.VideoViewModel
import edu.stanford.bdh.engagehf.modules.education.videos.VIDEO_SAVE_STATE_PARAM
import edu.stanford.bdh.engagehf.modules.education.videos.Video
import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent
import edu.stanford.bdh.engagehf.modules.navigation.Navigator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class VideoViewModelTest {

    private val navigator: Navigator = mockk(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
    private lateinit var viewModel: VideoViewModel

    @Before
    fun setUp() {
        val video = Video(
            title = "Test Video",
            description = "Test Description",
            orderIndex = 1,
            youtubeId = "testId"
        )
        val videoJson = Json.encodeToString(Video.serializer(), video)
        every { savedStateHandle.get<String>(VIDEO_SAVE_STATE_PARAM) } returns videoJson
        viewModel = VideoViewModel(navigator, savedStateHandle)
        every { navigator.navigateTo(NavigationEvent.PopBackStack) } returns Unit
    }

    @Test
    fun `when back pressed, navigate to pop up`() {
        // Given

        // When
        viewModel.onAction(Action.BackPressed)

        // Then
        verify { navigator.navigateTo(NavigationEvent.PopBackStack) }
    }
}

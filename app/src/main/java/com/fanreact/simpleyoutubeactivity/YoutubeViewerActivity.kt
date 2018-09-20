package com.fanreact.simpleyoutubeactivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_youtube_viewer.*
import java.lang.Exception

class YoutubeViewerActivity : YouTubeBaseActivity() {
    private var youtubeApiKey = ""
    private var youtubeUrl: String = ""
    private var videoId: String = ""
    private var startTime: Int = 0
    private var player: YouTubePlayer? = null

    private var genericErrorString = "Error launching Youtube video"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        youtubeApiKey = if (intent.hasExtra(KEY_YOUTUBE_API_KEY)) intent.getStringExtra(KEY_YOUTUBE_API_KEY)
            else if (savedInstanceState?.containsKey(KEY_YOUTUBE_API_KEY) == true) savedInstanceState.getString(KEY_YOUTUBE_API_KEY)
            else ""
        youtubeUrl = if (intent.hasExtra(KEY_YOUTUBE_URL)) intent.getStringExtra(KEY_YOUTUBE_URL)
            else if (savedInstanceState?.containsKey(KEY_YOUTUBE_URL) == true) savedInstanceState.getString(KEY_YOUTUBE_URL)
            else ""
        videoId = if (intent.hasExtra(KEY_ID)) intent.getStringExtra(KEY_ID)
            else if (savedInstanceState?.containsKey(KEY_ID) == true) savedInstanceState.getString(KEY_ID)
            else ""
        startTime =  if (savedInstanceState?.containsKey(KEY_START_TIME) == true) savedInstanceState.getInt(KEY_START_TIME, 0)
            else if (intent.hasExtra(KEY_START_TIME)) intent.getIntExtra(KEY_START_TIME, 0)
            else 0
        setContentView(R.layout.activity_youtube_viewer)
        if (videoId.isNotEmpty()) {
            loadYoutubePlayer(videoId, startTime)
        } else {
            getYoutubeVideoIdAndPlay(youtubeUrl)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        startTime = player?.currentTimeMillis ?: 0
        savedInstanceState?.apply {
            putString(KEY_YOUTUBE_API_KEY, youtubeApiKey)
            putString(KEY_ID, videoId)
            putString(KEY_YOUTUBE_URL, youtubeUrl)
            putInt(KEY_START_TIME, startTime)
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun isYoutube(url: String) = url.contains("youtube") || url.contains("youtu.be")
    private fun getYoutubeVideoIdAndPlay(url: String) {
        if (isYoutube(url)) {
            var startTime = this@YoutubeViewerActivity.startTime
            val videoId = if (url.contains("youtu.be")) {
                val substringAfterSlash = url.substringAfter("youtu.be/")
                if (substringAfterSlash.contains("?")) {
                    if (substringAfterSlash.contains("t=")) {
                        val substringAfterT = substringAfterSlash.substringAfter("t=")
                        startTime = try {
                            if (substringAfterT.contains("&")) {
                                substringAfterT.substringBefore("&").toInt()
                            } else {
                                substringAfterT.toInt()
                            }
                        } catch (e: Exception) {
                            startTime
                        }
                    }
                    substringAfterSlash.substringBefore("?")
                } else {
                    substringAfterSlash
                }
            } else if (url.contains("v=")) {
                val stringAfterV = url.substringAfter("v=")
                if (stringAfterV.contains("t=")) {
                    var timeString = stringAfterV.substringAfter("t=")
                    if (timeString.contains("&")) {
                        timeString = timeString.substringBefore("&")
                    }
                    startTime = try {
                        timeString.toInt()
                    } catch (e: Exception) {
                        startTime
                    }
                }
                if (stringAfterV.contains("&")) {
                    stringAfterV.substringBefore("&")
                } else {
                    stringAfterV
                }
            } else {
                null
            }
            videoId?.let {
                this.videoId = it
                this.startTime = startTime
                loadYoutubePlayer(it, startTime)
            } ?: run {
                Toast.makeText(this, genericErrorString, Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, genericErrorString, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadYoutubePlayer(youtubeVideoId: String, startTime: Int = 0) {
        playerSimpleYoutubeActivity.initialize(youtubeApiKey, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, b: Boolean) {
                this@YoutubeViewerActivity.player = player
                player?.apply {
                    loadVideo(youtubeVideoId, startTime)
                    play()
                    setOnFullscreenListener {
                        this@YoutubeViewerActivity.startTime = player.currentTimeMillis
                    }
                }
            }

            override fun onInitializationFailure(player: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                p1?.getErrorDialog(this@YoutubeViewerActivity, 1, { this@YoutubeViewerActivity.finish() })?.show()
            }
        })
    }

    companion object {
        private const val KEY_YOUTUBE_API_KEY = "KEY_YOUTUBE_API_KEY"
        private const val KEY_ID = "KEY_ID"
        private const val KEY_YOUTUBE_URL = "KEY_YOUTUBE_URL"
        private const val KEY_START_TIME = "KEY_START_TIME"

        fun startWithUrl(context: Context?, youtubeApiKey: String, youtubeUrl: String, startTime: Int = 0) = context?.startActivity(Intent(context, YoutubeViewerActivity::class.java).apply {
            putExtra(KEY_YOUTUBE_API_KEY, youtubeApiKey)
            putExtra(KEY_YOUTUBE_URL, youtubeUrl)
            putExtra(KEY_START_TIME, startTime)
        })

        fun startWithId(context: Context?, youtubeApiKey: String, videoId: String, startTime: Int = 0) = context?.startActivity(Intent(context, YoutubeViewerActivity::class.java).apply {
            putExtra(KEY_YOUTUBE_API_KEY, youtubeApiKey)
            putExtra(KEY_ID, videoId)
            putExtra(KEY_START_TIME, startTime)
        })

        fun getYoutubeVideoIdFromUrl(url: String) : String? {
            return if (url.contains("youtu.be") && !url.contains("youtube.com")) {
                val substringAfterSlash = url.substringAfter("youtu.be/")
                if (substringAfterSlash.contains("?")) {
                    substringAfterSlash.substringBefore("?")
                } else {
                    substringAfterSlash
                }
            } else if (url.contains("v=")) {
                val stringAfterV = url.substringAfter("v=")
                if (stringAfterV.contains("&")) {
                    stringAfterV.substringBefore("&")
                } else {
                    stringAfterV
                }
            } else {
                null
            }
        }
        fun getLargeYoutubeThumbnailFromUrl(fullUrl: String) : String? {
            return getYoutubeVideoIdFromUrl(fullUrl)?.let {
                "https://img.youtube.com/vi/$it/0.jpg"
            } ?: run {
                null
            }
        }

        fun getYoutubeThumbnailFromUrl(fullUrl: String) : String? {
            return getYoutubeVideoIdFromUrl(fullUrl)?.let {
                "https://img.youtube.com/vi/$it/1.jpg"
            } ?: run {
                null
            }
        }

        fun getLargeYoutubeThumbnailFromId(videoId: String) = "https://img.youtube.com/vi/$videoId/0.jpg"
        fun getYoutubeThumbnailFromId(videoId: String) = "https://img.youtube.com/vi/$videoId/1.jpg"
    }
}

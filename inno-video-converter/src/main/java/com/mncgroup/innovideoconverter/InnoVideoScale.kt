package com.mncgroup.innovideoconverter

/**
 * scale video
 * @property width scale widht of video, set -2 will tell app to automatically choose the correct width.
 * @property height scale height of video, set -2 will tell app to automatically choose the correct height.
 *
 * Example scale:
 * -2:720 : will tell app to scale height to 720 and thw width will automatically choose the correct width
 */
class InnoVideoScale(
    var width: Int,
    var height: Int
)
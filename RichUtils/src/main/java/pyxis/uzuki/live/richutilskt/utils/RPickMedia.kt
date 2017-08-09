@file:JvmName("RPickMedia")

package pyxis.uzuki.live.richutilskt.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class RPickMedia {

    private fun getActivity(context: Context): FragmentActivity? {
        var c = context

        while (c is ContextWrapper) {
            if (c is FragmentActivity) {
                return c
            }
            c = c.baseContext
        }
        return null
    }

    /**
     * pick image from Camera
     *
     * @param[callback] callback, should make class PickMediaCallback : PickMediaCallback
     */
    fun pickFromCamera(activity: FragmentActivity, callback: (Int, String) -> Unit) = requestPhotoPick(activity, PICK_FROM_CAMERA, callback)

    /**
     * pick image from Gallery
     *
     * @param[callback] callback, should make class PickMediaCallback : PickMediaCallback
     */
    fun pickFromGallery(activity: FragmentActivity, callback: (Int, String) -> Unit) = requestPhotoPick(activity, PICK_FROM_GALLERY, callback)

    /**
     * pick image from Video
     *
     * @param[callback] callback, should make class PickMediaCallback : PickMediaCallback
     */
    fun pickFromVideo(activity: FragmentActivity, callback: (Int, String) -> Unit) = requestPhotoPick(activity, PICK_FROM_VIDEO, callback)

    /**
     * pick image from Camera (Video Mode)
     *
     * @param[callback] callback, should make class PickMediaCallback : PickMediaCallback
     * @since 1.0.1
     */
    fun pickFromVideoCamera(activity: FragmentActivity, callback: (Int, String) -> Unit) = requestPhotoPick(activity, PICK_FROM_CAMERA_VIDEO, callback)

    private var currentPhotoPath: String? = null
    private var currentVideoPath: String? = null

    @SuppressLint("ValidFragment")
    internal fun requestPhotoPick(activity: FragmentActivity, pickType: Int, callback: (Int, String) -> Unit) {
        val fm = activity.supportFragmentManager
        val fragment = ResultFragment(fm as FragmentManager, callback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            fragment.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA), pickType)
            return
        }

        val intent = Intent()

        when (pickType) {
            PICK_FROM_CAMERA -> {
                intent.action = MediaStore.ACTION_IMAGE_CAPTURE
                val captureUri = createImageUri(activity)
                currentPhotoPath = captureUri.toString()
                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
            }

            PICK_FROM_GALLERY -> {
                intent.action = Intent.ACTION_PICK
                intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
            }

            PICK_FROM_VIDEO -> {
                intent.action = Intent.ACTION_PICK
                intent.type = android.provider.MediaStore.Video.Media.CONTENT_TYPE
            }

            PICK_FROM_CAMERA_VIDEO -> {
                intent.action = MediaStore.ACTION_VIDEO_CAPTURE
                val captureUri = createVideoUri(activity)
                currentVideoPath = captureUri.toString()
                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
            }
        }
        fragment.arguments.putString("currentPhotoPath", currentPhotoPath)
        fragment.arguments.putString("currentVideoPath", currentVideoPath)

        fm.beginTransaction().add(fragment, "FRAGMENT_TAG").commitNowAllowingStateLoss()
        fm.executePendingTransactions()

        fragment.startActivityForResult(intent, pickType)
    }

    private fun createImageUri(context: Context): Uri {
        val contentResolver = context.contentResolver
        val cv = ContentValues()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        cv.put(MediaStore.Images.Media.TITLE, timeStamp)
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
    }

    private fun createVideoUri(context: Context): Uri {
        val contentResolver = context.contentResolver
        val cv = ContentValues()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        cv.put(MediaStore.Images.Media.TITLE, timeStamp)
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv)
    }

    @SuppressLint("ValidFragment")
    inner class ResultFragment() : Fragment() {
        lateinit var fm: FragmentManager
        lateinit var callback: ((Int, String) -> Unit)

        constructor(fm: FragmentManager, callback: (Int, String) -> Unit) : this() {
            this.fm = fm
            this.callback = callback
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (verifyPermissions(grantResults)) {
                requestPhotoPick(activity, requestCode, callback)
            } else {
                callback.invoke(PICK_FAILED, "")
            }

            fm.beginTransaction()?.remove(this)?.commitAllowingStateLoss()

        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                PICK_FROM_CAMERA ->
                    if (resultCode == Activity.RESULT_OK)
                        currentPhotoPath?.let { callback.invoke(PICK_SUCCESS, Uri.parse(it) getRealPath (activity)) }

                PICK_FROM_GALLERY ->
                    if (resultCode == Activity.RESULT_OK)
                        callback.invoke(PICK_SUCCESS, data?.data?.getRealPath((activity)) as String)

                PICK_FROM_VIDEO ->
                    if (resultCode == Activity.RESULT_OK)
                        callback.invoke(PICK_SUCCESS, data?.data?.getRealPath((activity)) as String)

                PICK_FROM_CAMERA_VIDEO ->
                    if (resultCode == Activity.RESULT_OK) {
                        var path = data?.data?.getRealPath(activity) as String
                        if (path.isEmpty()) {
                            path = currentVideoPath as String
                        }

                        path.let {
                            callback.invoke(PICK_SUCCESS, path)
                        }
                    }
            }

            fm.beginTransaction()?.remove(this)?.commit()

        }

    }

    private fun verifyPermissions(grantResults: IntArray): Boolean =
            if (grantResults.isEmpty()) false else grantResults.none { it != PackageManager.PERMISSION_GRANTED }


    companion object {
        val PICK_FROM_CAMERA = 0
        val PICK_FROM_GALLERY = 1
        val PICK_FROM_VIDEO = 2
        val PICK_FROM_CAMERA_VIDEO = 3

        @JvmField val PICK_SUCCESS = 1
        @JvmField val PICK_FAILED = 0
    }
}
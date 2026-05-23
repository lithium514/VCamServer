package io.github.lithium514.vcamserver

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import java.net.NetworkInterface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.net.Inet4Address

class VCamHttpServer(private val context: Context, port: Int = 5000) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        return when {
            session.method == Method.POST && session.uri == "/upload" -> handleUpload(session)
            session.uri == "/" -> newFixedLengthResponse(
                Response.Status.OK, MIME_PLAINTEXT, "VCam Server is running"
            )
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found"
            )
        }
    }

    private fun handleUpload(session: IHTTPSession): Response {
        try {
            val files = HashMap<String, String>()
            session.parseBody(files)

            val filePath = files["image"]
                ?: return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    MIME_PLAINTEXT,
                    "Missing 'image' field in request"
                )

            val tempFile = File(filePath)
            if (!tempFile.exists() || tempFile.length() == 0L) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    MIME_PLAINTEXT,
                    "Uploaded image is empty"
                )
            }

            val uri = saveToGallery(tempFile)
            return newFixedLengthResponse(
                Response.Status.OK,
                MIME_PLAINTEXT,
                "Image saved successfully: $uri"
            )
        } catch (e: Exception) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "Error: ${e.message}"
            )
        }
    }

    private fun saveToGallery(file: File): String {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: throw IllegalStateException("Failed to decode image")

        val displayName = "VCam_${System.currentTimeMillis()}.jpg"
        val mimeType = "image/jpeg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/VCam")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Failed to create media entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, outputStream)
        } ?: throw IllegalStateException("Failed to open output stream")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        bitmap.recycle()
        return uri.toString()
    }

    companion object {
        fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            return address.hostAddress ?: "Unknown"
                        }
                    }
                }
            } catch (_: Exception) { }
            return "Unknown"
        }
    }
}

package com.nous.waterwell.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nous.waterwell.R
import com.nous.waterwell.ui.theme.LocalIosColors
import kotlinx.coroutines.*

@Composable
fun FullScreenQrDialog(onDismiss: () -> Unit) {
    val ios = LocalIosColors.current
    val ctx = LocalContext.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(end = 16.dp)) { Icon(Icons.Default.Close, "关闭", tint = Color.White, modifier = Modifier.size(32.dp)) }
                Spacer(Modifier.height(8.dp))
                Image(painter = painterResource(R.drawable.donate_qr), contentDescription = "微信收款码", modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Fit)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { saveQr(ctx) }, colors = ButtonDefaults.buttonColors(containerColor = ios.blue, contentColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Default.SaveAlt, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("保存到相册", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

private fun saveQr(ctx: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bmp = BitmapFactory.decodeResource(ctx.resources, R.drawable.donate_qr)
            val ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val v = ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, "AlexWater_${System.currentTimeMillis()}.jpg"); put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AlexWater") }
                ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v)?.let { ctx.contentResolver.openOutputStream(it)?.use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }; true } ?: false
            } else {
                @Suppress("DEPRECATION") val f = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AlexWater_收款码.jpg")
                f.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                MediaStore.Images.Media.insertImage(ctx.contentResolver, f.absolutePath, f.name, null); true
            }
            withContext(Dispatchers.Main) { Toast.makeText(ctx, if (ok) "已保存到相册 📸" else "保存失败", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(ctx, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show() } }
    }
}

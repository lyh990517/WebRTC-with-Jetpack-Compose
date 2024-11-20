package com.example.call.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageInputUi(onMessage: (ChatMessage) -> Unit, onInputChange: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var isAdditionalUiVisible by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<List<ImageBitmap>>(emptyList()) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddButton(isAdditionalUiVisible) {
                isAdditionalUiVisible = !isAdditionalUiVisible
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    onInputChange()
                },
                placeholder = { Text("Type a message...") },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            SendButton(
                selectedImage = selectedImage,
                onMessage = {
                    onMessage(it)
                    message = ""
                },
                message = message
            )
        }

        AdditionalUi(
            isAdditionalUiVisible = isAdditionalUiVisible,
            selectedImage = selectedImage
        ) {
            selectedImage = it
        }
    }
}

@Composable
private fun AddButton(isAdditionalUiVisible: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Icon(
            imageVector = if (isAdditionalUiVisible) Icons.Default.Close else Icons.Default.Add,
            contentDescription = if (isAdditionalUiVisible) "Close" else "Add",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SendButton(
    selectedImage: List<ImageBitmap>,
    onMessage: (ChatMessage) -> Unit,
    message: String
) {
    Button(
        onClick = {
            when {
                selectedImage.isNotEmpty() -> {
                    onMessage(
                        ChatMessage.Image(
                            type = ChatMessage.ChatType.ME,
                            images = selectedImage
                        )
                    )
                }

                else -> {
                    if (message.isNotBlank()) {
                        onMessage(
                            ChatMessage.Message(
                                type = ChatMessage.ChatType.ME,
                                message = message
                            )
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .height(48.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Send",
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            )
        }
    }
}

@Composable
private fun ColumnScope.AdditionalUi(
    isAdditionalUiVisible: Boolean,
    selectedImage: List<ImageBitmap>,
    onSelectImage: (List<ImageBitmap>) -> Unit
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onSelectImage(uris.mapNotNull { uri ->
            val stream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        })
    }

    AnimatedVisibility(
        visible = isAdditionalUiVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Select Image")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Image")
                }

                Button(
                    onClick = {
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Select File")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select File")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImage) { image ->
                    Image(
                        bitmap = image,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun MessageInputUiPreview() {
    MessageInputUi({}) { }
}
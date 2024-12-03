package com.example.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.call.ConnectionEvent

@Composable
fun ChatMessageInput(
    onEvent: (ConnectionEvent.Input) -> Unit,
) {
    var input by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MessageInput(
            input = input,
            onEvent = { inputEvent ->
                onEvent(inputEvent)
                input = inputEvent.input
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        SendButton {
            if (input.isNotBlank()) {
                onEvent(ConnectionEvent.Input.OnMessage(input))
                input = ""
            }
        }
    }
}

@Composable
private fun MessageInput(
    input: String,
    onEvent: (ConnectionEvent.Input.OnInputStateChanged) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = input,
        onValueChange = {
            onEvent(ConnectionEvent.Input.OnInputStateChanged(it))
        },
        placeholder = { Text("Type a message...") },
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        singleLine = true
    )
}

@Composable
private fun SendButton(onSend: () -> Unit) {
    Button(
        onClick = onSend,
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

@Preview
@Composable
fun MessageInputUiPreview() {
    ChatMessageInput({})
}

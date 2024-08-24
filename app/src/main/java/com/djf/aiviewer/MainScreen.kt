package com.djf.aiviewer

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun MainScreen(
    discoverViewModel: AppViewModel = viewModel(),
) {

    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by discoverViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var uriLocal: Uri? by rememberSaveable {
        mutableStateOf(null)
    }
    val painter = rememberAsyncImagePainter(
        ImageRequest
            .Builder(LocalContext.current)
            .data(data = uriLocal)
            .build()
    )

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                uriLocal = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    // Launch the photo picker and let the user choose only images/videos of a
// specific MIME type, such as GIFs.
    fun launchPhotoPicker() {
        val mimeType = "image/*"
        pickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.SingleMimeType(
                    mimeType
                )
            )
        )

    }



    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        uriLocal?.let {
            Image(
                contentScale = ContentScale.Crop,
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .padding(5.dp)
                    .size(200.dp)
                    .clip(RoundedCornerShape(15.dp))
            )
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(onClick = { launchPhotoPicker() }, modifier = Modifier.padding(5.dp)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = " add photo")
                Text(text = "pick photo")
            }
        }
        Text(text = stringResource(id = R.string.ask_gemini_about_your_photo), style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            TextField(
                value = prompt,
                label = { Text(stringResource(R.string.label_prompt)) },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    val paintState = painter.state
                    if (paintState is AsyncImagePainter.State.Success) {
                        discoverViewModel.sendPrompt(
                            bitmap = paintState.result.drawable.toBitmap(),
                            prompt = prompt
                        )
                    }
                },
                enabled = prompt.isNotEmpty(),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.action_go))
            }
        }
        HorizontalDivider()
        if (uiState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
            } else if (uiState is UiState.Success) {
                textColor = MaterialTheme.colorScheme.onSurface
                result = (uiState as UiState.Success).outputText
            }
            val scrollState = rememberScrollState()
            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }
    }
}







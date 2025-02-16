package com.idk.portagalwithai

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idk.portagalwithai.ui.theme.PortagalWithAiTheme
import com.idk.portagalwithai.ui.theme.PrimaryColor
import kotlinx.coroutines.delay
import me.nikhilchaudhari.library.neumorphic
import me.nikhilchaudhari.library.shapes.Punched
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager

val images = arrayOf(
    // Image generated using Gemini from the prompt "cupcake image"
    R.drawable.baked_goods_1,
    // Image generated using Gemini from the prompt "cookies images"
    R.drawable.baked_goods_2,
    // Image generated using Gemini from the prompt "cake images"
    R.drawable.baked_goods_3,
)
val imageDescriptions = arrayOf(
    R.string.image1_description,
    R.string.image2_description,
    R.string.image3_description,
)

@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current


    Greeting()

}

@Composable
fun Wait(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.baking_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(images) { index, image ->
                var imageModifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .requiredSize(200.dp)
                    .clickable {
                        selectedImage.intValue = index
                    }
                if (index == selectedImage.intValue) {
                    imageModifier =
                        imageModifier.border(BorderStroke(4.dp, MaterialTheme.colorScheme.primary))
                }
                Image(
                    painter = painterResource(image),
                    contentDescription = stringResource(imageDescriptions[index]),
                    modifier = imageModifier
                )
            }
        }

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
                    val bitmap = BitmapFactory.decodeResource(
                        context.resources,
                        images[selectedImage.intValue]
                    )
                    bakingViewModel.sendPrompt(bitmap, prompt)
                },
                enabled = prompt.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.action_go))
            }
        }

        // response
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    bakingViewModel: BakingViewModel = viewModel()
) {

//    val generativeModel = GenerativeModel(
//        // For text-only input, use the gemini-pro model
//        modelName = "gemini-pro",
//        // Access your API key as a Build Configuration variable (see "Set up your API key" above)
//        apiKey = BuildConfig.apiKey
//    )


    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var copiedMessage by remember { mutableStateOf("") }


    var isMoved by remember {
        mutableStateOf(false)
    }


    val offsetX by animateFloatAsState(targetValue = if (isMoved) -50f else 0f, label = "")


    val avatarList = listOf(
        Mood(
            title = "Happy",
            avatar = "\uD83D\uDE03"
        ),
        Mood(
            title = "Sad",
            avatar = "\uD83E\uDD72"
        ),
        Mood(
            title = "In love",
            avatar = "\uD83D\uDE0D"
        ),
        Mood(
            title = "Angry",
            avatar = "\uD83E\uDD2C"
        ),
        Mood(
            title = "Need attention",
            avatar = "\uD83E\uDD7A"
        ),
        Mood(
            "Sick",
            avatar = "\uD83E\uDD12"
        )
    )

    val pagerState = rememberPagerState(pageCount = {
        avatarList.size
    })


    var expandedMoodView by remember { mutableStateOf(false) }

    var isColorFull by remember { mutableStateOf(true) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isColorFull) PrimaryColor else Color.Transparent,
        animationSpec = tween(durationMillis = 500)
    )

    var rotated by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f
    )

    var isBlurred by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            images[selectedImage.intValue]
        )
        bakingViewModel.sendPrompt(bitmap, prompt)

        delay(1000)

        expandedMoodView = !expandedMoodView
        isColorFull = !isColorFull
        rotated = !rotated

        delay(2000)


        isMoved = true

        delay(500)

        isMoved = false

    }



    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = 42.dp,
                    end = 8.dp,
                    start = 8.dp
                )
                .animateContentSize()
                .height(if (expandedMoodView) 300.dp else 35.dp)
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))

        ) {
            // title of pager
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expandedMoodView = !expandedMoodView
                        isColorFull = !isColorFull
                        rotated = !rotated
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "How you doing?",
                    fontSize = 26.sp,
                    color = if (expandedMoodView) MaterialTheme.colorScheme.onBackground else Color.White
                )
                Icon(
                    modifier = Modifier.graphicsLayer(rotationZ = rotationState),
                    painter = painterResource(id = R.drawable.ic_down),
                    contentDescription = null,
                    tint = if (expandedMoodView) MaterialTheme.colorScheme.onBackground else Color.White
                )

            }


            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(top = 60.dp)
                    .offset(x = offsetX.dp)


            ) { page ->

                // Our page content
                ShowMood(
                    avatar = avatarList[page].avatar,
                    title = avatarList[page].title
                )

            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // add note box
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(24.dp)
                    .neumorphic(
                        neuShape = Punched.Oval()
                    )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Add note")
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note),
                        contentDescription = null
                    )
                }
            }
        }

        // recommended notes...
        Card(
            onClick = {
                if (result.isNotEmpty()){
                    // Create an intent to share text
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, result)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share text via"))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            // Copy text to clipboard
                            val clip = android.content.ClipData.newPlainText("label", result)
                            clipboard.setPrimaryClip(clip)
                            copiedMessage = "Copied: $result"
                        }
                    )
                }
                .neumorphic(
//                    neuShape = Punched.Oval()
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE9E8E9)
            ),
        ) {
            // response
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }

            if (copiedMessage.isNotEmpty()) {
                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                copiedMessage = ""
            }

        }


    }


}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun GreetingPreview() {
    PortagalWithAiTheme {
        Greeting()
    }
}

// each mode box
@Composable
fun ShowMood(
    avatar: String,
    title: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            onClick = {
                // todo
            },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE9E8E9)
            ),
            shape = CircleShape,
            modifier = Modifier
                .neumorphic(
                    neuShape = Punched.Oval()
                )
        ) {
            Column(
                modifier = Modifier.padding(19.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = avatar,
                    fontSize = 100.sp,
                )

            }

        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title
        )

    }

}
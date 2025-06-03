package com.example.feedup.ui.screens


import AuthViewModel
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feedup.R
import com.example.feedup.ui.components.PrimaryButton
import com.example.feedup.ui.themes.CardShape
import com.example.feedup.ui.themes.secondary
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.xLargePadding
import com.example.feedup.ui.themes.xSmallPadding
import com.example.feedup.utils.lerp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun BoardingScreen(authViewModel : AuthViewModel){

    data class BoardingData(
        val imageResId: Int,
        val title: String,
        val description: String,
        val isLoginPage: Boolean
    )

    val pages = listOf(
        BoardingData(
            imageResId = R.drawable.cooking_image1,
            title = "Welcome",
            description = "Découvrez comment préparer les plats locaux de votre contrée.",
            isLoginPage = false
        ),
        BoardingData(
            imageResId = R.drawable.cooking_image2,
            title = "Learn To Cook",
            description = "Avec nos recettes détaillées, impossible de se tromper.",
            isLoginPage = false
        ),
        BoardingData(
            imageResId = R.drawable.cooking_image3,
            title = "Collaboration",
            description = "Nous collaborons avec votre comfort.",
            isLoginPage = false
        ),
        BoardingData(
            imageResId = R.drawable.cooking_image4,
            title = "",
            description = "",
            isLoginPage = true
        )
    )

    val pagerState = rememberPagerState { pages.size }
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val sideScale   = 0.8f           // 80 % pour les pages voisines
    val sideOverlap = 0.10f          // 10 % visibles sur chaque bord

    Scaffold(containerColor = Color.Transparent)
    { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            HorizontalPager(
                state = pagerState,
                pageSpacing = xLargePadding,
                contentPadding = PaddingValues(
                    horizontal = LocalConfiguration.current.screenWidthDp.dp * sideOverlap
                ),
                pageSize = PageSize.Fill,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) { page ->

                // Décalage de -1 (gauche) à 0 (centrale) à +1 (droite)
                val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val norm = offset.coerceIn(-1f, 1f)

                val scale = lerp(1f, sideScale, abs(norm))
                val translateX = screenWidthPx * sideOverlap * norm

                Card(
                    modifier = Modifier
                        .height(450.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = translateX
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        },

                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Image(
                        painter = painterResource(id = pages[page].imageResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(smallPadding))

            // Dots Indicator
            PagerGliderDots(pageCount = pages.size, pagerState)


            if (!pages[pagerState.currentPage].isLoginPage) {
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(
                    targetState = pagerState.currentPage,
                    transitionSpec = {
                        (slideInVertically(
                            animationSpec = tween(400, 100, easing = FastOutSlowInEasing)
                        ) { -it / 2 } + fadeIn(tween(400, 100))
                                togetherWith
                                slideOutVertically(
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) { it / 2 } + fadeOut(tween(400)))
                    },
                    contentAlignment = Alignment.TopCenter
                ) { page ->

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            pages[page].title,
                            style = MaterialTheme.typography.displayMedium
                        )

                        Spacer(Modifier.height(xSmallPadding))

                        Text(
                            pages[page].description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = secondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            if (pages[pagerState.currentPage].isLoginPage) {
                Spacer(modifier = Modifier.height(8.dp))
                LoginForm(authViewModel)
            }

        }
    }
}

@Composable
fun LoginForm(
    authViewModel : AuthViewModel
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            if (isLoginMode) "Connection"
            else "Inscription",
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) painterResource(R.drawable.eye_line) else painterResource(R.drawable.no_eye_line)
                val description = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = image, contentDescription = description)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            enabled = email.isNotEmpty() && password.isNotEmpty(),
            onClick = {
                val auth = FirebaseAuth.getInstance()
                if (isLoginMode) {

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            val message = if (task.isSuccessful)
                                "Connexion réussie!"
                            else
                                "Erreur: ${task.exception?.message}"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            val message = if (task.isSuccessful)
                                "Inscription réussie!"
                            else
                                "Erreur: ${task.exception?.message}"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                }
            },
            text = if (isLoginMode) "Se connecter" else "S'inscrire"
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                if (isLoginMode) "Pas encore de compte ? S'inscrire"
                else "Déjà un compte ? Se connecter",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PagerGliderDots(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    dotDiameter: Dp = 12.dp,
    ringDiameter: Dp = 12.dp,                // diamètre du cercle “repère”
    ringStroke: Dp = 1.dp,
    spacing: Dp = 12.dp,                     // écart entre cercles
    activeColor: Color = MaterialTheme.colorScheme.primary,
    ringColor: Color = MaterialTheme.colorScheme.secondary,
) {
    val scope = rememberCoroutineScope()

    val ringsWidth = remember(pageCount, ringDiameter, spacing) {
        (pageCount * ringDiameter.value.roundToInt() + (pageCount - 1) * spacing.value.roundToInt()).dp
    }

    val animatedPage by animateFloatAsState(
        targetValue = pagerState.currentPage.toFloat(),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "dotSlide"
    )

    Box(
        modifier = modifier
            .padding(16.dp)
            .width(ringsWidth)
            .height(ringDiameter)
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            repeat(pageCount) {
                Box(
                    Modifier
                        .size(ringDiameter)
                        .border(ringStroke, ringColor, CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch { pagerState.animateScrollToPage(it) }
                        }
                )
            }
        }


        val offsetX = with(LocalDensity.current) {
            (ringDiameter.toPx() + spacing.toPx()) * animatedPage
        }

        Canvas(
            Modifier
                .size(ringDiameter)
                .graphicsLayer { translationX = offsetX },
        ) {
            drawCircle(
                color = activeColor,
                radius = dotDiameter.toPx() / 2
            )
        }
    }
}
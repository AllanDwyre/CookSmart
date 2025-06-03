package com.example.feedup.ui.screens

import AuthViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.feedup.data.local.entities.UserProfile
import com.example.feedup.ui.components.DualButtonRow
import com.example.feedup.ui.components.PrimaryButton
import com.example.feedup.ui.components.ProgressBar
import com.example.feedup.ui.components.SelectableGrid
import com.example.feedup.ui.components.SelectionMode
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.xLargePadding
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

fun updateProfile(
    profileData: UserProfile,
    authViewModel: AuthViewModel,
    name :String,
    diet : String,
    allergies : List<String>,
    goals: List<String>,
){
    val finalProfile = profileData.copy(
        displayName = name,
        dietaryPreferences = diet,
        allergies = allergies,
        goals = goals,
        isProfileComplete = true
    )

    authViewModel.updateUserProfile(finalProfile)
}

@Composable
fun PreferenceScreen(
    userProfile: UserProfile?,
    authViewModel: AuthViewModel,
    navController: NavHostController? = null,
    user: FirebaseUser
){
    var name by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var selectedDietPlan by remember { mutableStateOf(setOf(userProfile?.dietaryPreferences ?: "")) }
    var selectedAllergy by remember { mutableStateOf(userProfile?.allergies?.toSet() ?: setOf("")) }
    var selectedGoal by remember { mutableStateOf(userProfile?.goals?.toSet() ?: setOf("")) }

    val focusRequester = remember { FocusRequester() }

    var profileData by remember {
        mutableStateOf(
            userProfile ?: UserProfile()
        )
    }

    data class StepData(
        val title: AnnotatedString,
        val description: String,
        val stepView: @Composable () -> Unit
    )

    val steps = listOf<StepData>(
        StepData(
           title = buildAnnotatedString {
               append("What is your ")
               withStyle(style = SpanStyle(color = accent)) {
                   append("Name")
               }
               append("?")
           },
            description = "It's important to know your name.",
            stepView = {
                OutlinedTextField(
                    value = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    onValueChange = {name = it},
                    singleLine = true,
                    label = { Text("Name") }

                )
            }
        ),
        StepData(
            title = buildAnnotatedString {
                append("What is your ")
                withStyle(style = SpanStyle(color = accent)) {
                    append("diet plan")
                }
                append("?")
            },
            description = "Select what’s important to you.",
            stepView = {
                SelectableGrid(
                    items = listOf("Végétarien", "Végétalien", "Sans gluten", "Végan", "Keto"),
                    selectedItems = selectedDietPlan,
                    onSelectionChange = {selectedDietPlan = it},
                    selectionMode = SelectionMode.SINGLE,
                )
            }
        ),
        StepData(
            title = buildAnnotatedString {
                append("Are you ")
                withStyle(style = SpanStyle(color = accent)) {
                    append("allergic")
                }
                append(" to any product?")
            },
            description = "We’ll only show you recipes that you can eat.",
            stepView = {
                SelectableGrid(
                    items = listOf("Gluten", "Dairy", "Egg", "Soy", "Peenut", "Treenut", "Fish", "Shellfish"),
                    selectedItems = selectedAllergy,
                    onSelectionChange = {selectedAllergy = it},
                )
            }
        ),
        StepData(
            title = buildAnnotatedString {
                append("What is your ")
                withStyle(style = SpanStyle(color = accent)) {
                    append("goal")
                }
                append("?")
            },
            description = "Select what’s important to you.",
            stepView = {
                SelectableGrid(
                    items = listOf("Eat healthy", "Budget-Friendly", "Plan better", "Learn to cook", "Quick & Easy"),
                    selectedItems = selectedGoal,
                    onSelectionChange = {selectedGoal = it},
                )
            }
        )
    )

    val stepsState = rememberPagerState { steps.size }
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    LaunchedEffect(stepsState.currentPage) {
        if (stepsState.currentPage == 0) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(mediumPadding)
    ) {
        Spacer(modifier = Modifier.height(mediumPadding))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressBar(
                progress = (stepsState.currentPage.toFloat() + 1f + stepsState.currentPageOffsetFraction) / stepsState.pageCount.toFloat(),
                modifier = Modifier.weight(1f)
            )

            if (stepsState.currentPage != 0) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        updateProfile(
                            profileData,
                            authViewModel,
                            name,
                            selectedDietPlan.first(),
                            selectedAllergy.toList(),
                            selectedGoal.toList()
                        )
                        navController?.navigateUp()
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("Skip", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(mediumPadding))
        HorizontalPager(
            state = stepsState,
            pageSpacing = xLargePadding,
            userScrollEnabled = name.isNotEmpty(),
            pageSize = PageSize.Fill,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { stepIndex ->
            val step = steps[stepIndex]
            Column {
                Text(text = step.title, style = MaterialTheme.typography.titleLarge)
                Text(text = step.description, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                step.stepView()
                Spacer(Modifier.weight(2f))
            }
        }

        if (stepsState.currentPage == 0 && navController == null) {
            PrimaryButton(
                text = if (stepsState.currentPage == stepsState.pageCount - 1) "Done" else "Next",
                onClick = {
                    if (stepsState.currentPage == stepsState.pageCount - 1)
                        updateProfile(
                            profileData,
                            authViewModel,
                            name,
                            selectedDietPlan.first(),
                            selectedAllergy.toList(),
                            selectedGoal.toList()
                        )
                    else {
                        scope.launch { stepsState.animateScrollToPage(stepsState.currentPage + 1) }
                    }
                },
                enabled = name.isNotEmpty()
            )
        } else {
            DualButtonRow(
                leftText = "Go back",
                onLeftClick =
                {
                    if (stepsState.currentPage == 0)
                        navController?.navigateUp()

                    scope.launch { stepsState.animateScrollToPage(stepsState.currentPage - 1) }
                },
                rightText = if (stepsState.currentPage == stepsState.pageCount - 1) "Done" else "Next",
                onRightClick = {
                    if (stepsState.currentPage == stepsState.pageCount - 1) {
                        updateProfile(
                            profileData,
                            authViewModel,
                            name,
                            selectedDietPlan.first(),
                            selectedAllergy.toList(),
                            selectedGoal.toList()
                        )
                        navController?.navigateUp()
                    }
                    else {
                        scope.launch { stepsState.animateScrollToPage(stepsState.currentPage + 1) }
                    }
                },
                enabled = name.isNotEmpty()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

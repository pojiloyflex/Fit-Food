package com.dviss.calorietracker

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dviss.calorietracker.navigation.Route
import com.dviss.calorietracker.repository.TrackerRepositoryFake
import com.dviss.calorietracker.ui.theme.CalorieTrackerTheme
import com.dviss.core.domain.model.ActivityLevel
import com.dviss.core.domain.model.Gender
import com.dviss.core.domain.model.GoalType
import com.dviss.core.domain.model.UserInfo
import com.dviss.core.domain.preferences.Preferences
import com.dviss.core.domain.usecase.FilterOutDigits
import com.dviss.tracker_domain.model.TrackableFood
import com.dviss.tracker_domain.usecase.*
import com.dviss.tracker_presentation.search.SearchScreen
import com.dviss.tracker_presentation.search.SearchViewModel
import com.dviss.tracker_presentation.tracker_overview.TrackerOverviewScreen
import com.dviss.tracker_presentation.tracker_overview.TrackerOverviewViewmodel
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.roundToInt

@HiltAndroidTest
class TrackerOverviewE2E {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var repository: TrackerRepositoryFake
    private lateinit var trackerUseCases: TrackerUseCases
    private lateinit var preferences: Preferences
    private lateinit var trackerOverviewViewmodel: TrackerOverviewViewmodel
    private lateinit var searchViewModel: SearchViewModel

    private lateinit var navController: NavHostController

    @Before
    fun setUp() {
        preferences = mockk(relaxed = true)
        every { preferences.loadUserInfo() } returns UserInfo(
            gender = Gender.Male,
            age = 20,
            weight = 80f,
            height = 180,
            activityLevel = ActivityLevel.Medium,
            goalType = GoalType.KeepWeight,
            carbRatio = 0.4f,
            proteinRatio = 0.3f,
            fatRatio = 0.3f
        )
        repository = TrackerRepositoryFake()
        trackerUseCases = TrackerUseCases(
            trackFood = TrackFood(repository),
            searchFood = SearchFood(repository),
            getFoodsForDate = GetFoodsForDate(repository),
            deleteTrackedFood = DeleteTrackedFood(repository),
            calculateMealNutrients = CalculateMealNutrients(preferences)
        )
        trackerOverviewViewmodel = TrackerOverviewViewmodel(
            preferences = preferences,
            trackerUseCases = trackerUseCases
        )
        searchViewModel = SearchViewModel(
            trackerUseCases = trackerUseCases,
            filterOutDigits = FilterOutDigits()
        )
        composeRule.activity.setContent {
            CalorieTrackerTheme {
                val scaffoldState = rememberScaffoldState()
                navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    scaffoldState = scaffoldState,
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Route.TRACKER_OVERVIEW,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Route.TRACKER_OVERVIEW) {
                            TrackerOverviewScreen(
                                onNavigateToSearch = { mealName, day, month, year ->
                                    navController.navigate(
                                        Route.SEARCH +
                                                "/$mealName" + "/$day" + "/$month" + "/$year"
                                    )
                                },
                                viewmodel = trackerOverviewViewmodel
                            )
                        }
                        composable(
                            route = Route.SEARCH + "/{mealName}/{dayOfMonth}/{month}/{year}",
                            arguments = listOf(
                                navArgument("mealName") {
                                    type = NavType.StringType
                                },
                                navArgument("dayOfMonth") {
                                    type = NavType.IntType
                                },
                                navArgument("month") {
                                    type = NavType.IntType
                                },
                                navArgument("year") {
                                    type = NavType.IntType
                                }
                            )
                        ) {
                            val mealName = it.arguments?.getString("mealName")!!
                            val dayOfMonth = it.arguments?.getInt("dayOfMonth")!!
                            val month = it.arguments?.getInt("month")!!
                            val year = it.arguments?.getInt("year")!!
                            SearchScreen(
                                scaffoldState = scaffoldState,
                                mealName = mealName,
                                dayOfMonth = dayOfMonth,
                                month = month,
                                year = year,
                                onNavigateUp = {
                                    navController.navigateUp()
                                },
                                viewModel = searchViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun addBreakfast_appearsUnderBreakfast_nutrientsProperlyCalculated() {
        repository.searchResults = listOf(
            TrackableFood(
                name = "banana",
                imageUrl = null,
                caloriesPer100g = 150,
                carbsPer100g = 50,
                proteinPer100g = 5,
                fatPer100g = 1
            )
        )
        val addedAmount = 150

        val expectedCalories = (1.5f * 150).roundToInt()
        val expectedCarbs = (1.5f * 50).roundToInt()
        val expectedProtein = (1.5f * 5).roundToInt()
        val expectedFat = (1.5f * 1).roundToInt()

        //Check that there is no add button yet
        composeRule
            .onNodeWithText("Add Breakfast")
            .assertDoesNotExist()
        //Click on meal to expand it
        composeRule
            .onNodeWithContentDescription("Breakfast")
            .performClick()
        //Check that add button appeared
        composeRule
            .onNodeWithText("Add Breakfast")
            .assertIsDisplayed()
        //Press add button
        composeRule
            .onNodeWithText("Add Breakfast")
            .performClick()
        //Check that navigated to search screen
        assertThat(
            navController
                .currentDestination
                ?.route
                ?.startsWith(Route.SEARCH)
        ).isTrue()
        //input query into search field
        composeRule
            .onNodeWithTag("search_textfield")
            .performTextInput("banana")
        //click search
        composeRule
            .onNodeWithContentDescription("Search...")
            .performClick()
        //click on some search result
        composeRule
            .onNodeWithText("Carbs")
            .performClick()
        //populate amount
        composeRule
            .onNodeWithContentDescription("Amount")
            .performTextInput(addedAmount.toStr())
        //click track
        composeRule
            .onNodeWithContentDescription("Track")
            .performClick()
        //check if app navigated back to overview screen
        assertThat(
            navController
                .currentDestination
                ?.route
                ?.startsWith(Route.TRACKER_OVERVIEW)
        ).isTrue()
        //check if added carbs are displayed
        composeRule
            .onAllNodesWithText(expectedCarbs.toStr())
            .onFirst()
            .assertIsDisplayed()
        //check if added protein is displayed
        composeRule
            .onAllNodesWithText(expectedProtein.toStr())
            .onFirst()
            .assertIsDisplayed()
        //check if added fat is displayed
        composeRule
            .onAllNodesWithText(expectedFat.toStr())
            .onFirst()
            .assertIsDisplayed()
        //check if added calories are displayed
        composeRule
            .onAllNodesWithText(expectedCalories.toStr())
            .onFirst()
            .assertIsDisplayed()
    }
}
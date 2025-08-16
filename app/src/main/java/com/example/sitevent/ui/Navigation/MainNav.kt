package com.example.sitevent.ui.Navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.sitevent.data.Resource
import com.example.sitevent.ui.screen.Auth.LoginScreen
import com.example.sitevent.ui.screen.Auth.SignUpScreen
import com.example.sitevent.ui.screen.Auth.VerifyEmailScreen
import com.example.sitevent.ui.screen.ChatScreen
import com.example.sitevent.ui.screen.Club.AllClubScreen
import com.example.sitevent.ui.screen.Club.ClubDetailScreen
import com.example.sitevent.ui.screen.Club.ClubSettingScreen
import com.example.sitevent.ui.screen.Club.CreateClubScreen
import com.example.sitevent.ui.screen.Club.EditClubDetailScreen
import com.example.sitevent.ui.screen.Club.Event.AllTeamsForEventScreen
import com.example.sitevent.ui.screen.Club.Event.CreateEventScreen
import com.example.sitevent.ui.screen.Club.Event.EventDetailScreen
import com.example.sitevent.ui.screen.Club.Event.EventRegistrationScreen
import com.example.sitevent.ui.screen.Club.Event.EventSettingScreen
import com.example.sitevent.ui.screen.Club.Event.TeamDetailedScreen
import com.example.sitevent.ui.screen.Club.ManageClubRolesScreen
import com.example.sitevent.ui.screen.ClubCategory.AllCategoryScreen
import com.example.sitevent.ui.screen.ClubCategory.ClubCategorySettingScreen
import com.example.sitevent.ui.screen.ClubCategory.CreateCategoryScreen
import com.example.sitevent.ui.screen.ClubCategory.SingleCategoryScreen
import com.example.sitevent.ui.screen.HomeScreen
import com.example.sitevent.settings.PrivacyPolicyScreen
import com.example.sitevent.ui.screen.ProfileScreen
import com.example.sitevent.settings.UserTicketDetailedScreen
import com.example.sitevent.ui.screen.User.UserTicketScreen
import com.example.sitevent.ui.screen.ReportBugScreen
import com.example.sitevent.ui.viewModel.AuthViewModel
import com.example.sitevent.ui.viewModel.UserViewModel

enum class Screen {
    LOGIN,
    SIGNUP,
    VERIFY,

    CREATE_CATEGORY_SCREEN,
    ALL_CATEGORY_SCREEN,
    SINGLE_CATEGORY_SCREEN,
    CATEGORY_SETTING_SCREEN,

    CREATE_EVENT_SCREEN,
    EVENT_DETAIL_SCREEN,
    EVENT_REGISTRATION_SCREEN,
    EVENT_SETTING_SCREEN,
    ALL_TEAMS_FOR_EVENT,
    TEAM_DETAIL_SCREEN,

    //bottom bar
    HOME,
    All_CLUBS_SCREEN,
    CLUB_DETAIL_SCREEN,
    CREATE_CLUB_SCREEN,
    EDIT_CLUB_SCREEN,
    CLUB_SETTING_SCREEN,
    MANAGE_CLUB_ROLES,
    CHATS,
    PROFILE,

    //User
    User_TICKET_SCREEN,
    User_EVENT_SCREEN,
    User_CLUB_SCREEN,
    EDIT_PROFILE_SCREEN,
    USER_TICKET_DETAIL_SCREEN,


    // Privacy Policy
    PRIVACY_POLICY_SCREEN,

    // Report Bug
    REPORT_BUG_SCREEN,
}

sealed class NavigationItem(val route: String) {
    object Login : NavigationItem(Screen.LOGIN.name)
    object SignUp : NavigationItem(Screen.SIGNUP.name)
    object Verify : NavigationItem(Screen.VERIFY.name)
    object Home : NavigationItem(Screen.HOME.name)

    object CreateCategory : NavigationItem(Screen.CREATE_CATEGORY_SCREEN.name)
    object AllCategory : NavigationItem(Screen.ALL_CATEGORY_SCREEN.name)
    object SingleCategory : NavigationItem("${Screen.SINGLE_CATEGORY_SCREEN.name}/{categoryId}") {
        fun createRoute(categoryId: String) = "${Screen.SINGLE_CATEGORY_SCREEN.name}/$categoryId"
        fun createDeepLink(categoryId: String) = "sitevent://app/${createRoute(categoryId)}"
    }
    object CategorySetting : NavigationItem("${Screen.CATEGORY_SETTING_SCREEN.name}/{categoryId}") {
        fun createRoute(categoryId: String) = "${Screen.CATEGORY_SETTING_SCREEN.name}/$categoryId"
        fun createDeepLink(categoryId: String) = "sitevent://app/${createRoute(categoryId)}"
    }

    object CreateClub : NavigationItem("${Screen.CREATE_CLUB_SCREEN.name}/{categoryId}") {
        fun createRoute(categoryId: String) = "${Screen.CREATE_CLUB_SCREEN.name}/$categoryId"
        fun createDeepLink(categoryId: String) = "sitevent://app/${createRoute(categoryId)}"
    }

    object CreateEvent :
        NavigationItem("${Screen.CREATE_EVENT_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.CREATE_EVENT_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "sitevent://app/${createRoute(categoryId, clubId)}"
    }

    object EventDetail : NavigationItem(
        route = "${Screen.EVENT_DETAIL_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
    ) {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.EVENT_DETAIL_SCREEN.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "sitevent://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object EventRegistration :
        NavigationItem("${Screen.EVENT_REGISTRATION_SCREEN.name}/{categoryId}/{clubId}/{eventId}") {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.EVENT_REGISTRATION_SCREEN.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "sitevent://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object EventSetting : NavigationItem("${Screen.EVENT_SETTING_SCREEN.name}/{categoryId}/{clubId}/{eventId}") {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.EVENT_SETTING_SCREEN.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "sitevent://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object AllTeamsForEvent : NavigationItem("${Screen.ALL_TEAMS_FOR_EVENT.name}/{categoryId}/{clubId}/{eventId}") {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.ALL_TEAMS_FOR_EVENT.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "sitevent://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object TeamDetail : NavigationItem("${Screen.TEAM_DETAIL_SCREEN.name}/{ticketId}/{teamId}") {
        fun createRoute(ticketId: String, teamId: String) =
            "${Screen.TEAM_DETAIL_SCREEN.name}/$ticketId/$teamId"
        fun createDeepLink(ticketId: String, teamId: String) = "sitevent://app/${createRoute(ticketId, teamId)}"
    }

    object AllClubs : NavigationItem(Screen.All_CLUBS_SCREEN.name)
    object ClubDetail : NavigationItem("${Screen.CLUB_DETAIL_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.CLUB_DETAIL_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "sitevent://app/${createRoute(categoryId, clubId)}"
    }

    object ClubSetting : NavigationItem("${Screen.CLUB_SETTING_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.CLUB_SETTING_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "sitevent://app/${createRoute(categoryId, clubId)}"
    }

    object EditClub : NavigationItem("${Screen.EDIT_CLUB_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.EDIT_CLUB_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "sitevent://app/${createRoute(categoryId, clubId)}"
    }

    object ManageClubRoles : NavigationItem("${Screen.MANAGE_CLUB_ROLES.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.MANAGE_CLUB_ROLES.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "sitevent://app/${createRoute(categoryId, clubId)}"
    }

    object Chats : NavigationItem(Screen.CHATS.name)
    object Profile : NavigationItem(Screen.PROFILE.name)

    //User
    object UserTicket : NavigationItem(Screen.User_TICKET_SCREEN.name)
    object UserEvent : NavigationItem(Screen.User_EVENT_SCREEN.name)
    object UserClub : NavigationItem(Screen.User_CLUB_SCREEN.name)
    object EditProfile : NavigationItem(Screen.EDIT_PROFILE_SCREEN.name)
    object UserTicketDetail :
        NavigationItem("${Screen.USER_TICKET_DETAIL_SCREEN.name}/{userId}/{ticketId}") {
        fun createRoute(userId: String, ticketId: String) =
            "${Screen.USER_TICKET_DETAIL_SCREEN.name}/$userId/$ticketId"
        fun createDeepLink(userId: String, ticketId: String) = "sitevent://app/${createRoute(userId, ticketId)}"
    }


    object PrivacyPolicy : NavigationItem(Screen.PRIVACY_POLICY_SCREEN.name)
    object ReportBug : NavigationItem(Screen.REPORT_BUG_SCREEN.name)
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val currentUserRes by userViewModel.observeUser.collectAsState()
    val role = when (currentUserRes) {
        is Resource.Loading -> "Loading…"
        is Resource.Error -> "Error"
        is Resource.Success -> (currentUserRes as Resource.Success).data?.appRole?.toString() ?: "Unknown"
        else -> "…"
    }

    val userId = when (currentUserRes) {
        is Resource.Success -> (currentUserRes as Resource.Success).data?.email ?: ""
        else -> ""
    }

    val startDestination = if (authViewModel.signInStatus()) NavigationItem.Home.route
    else NavigationItem.Login.route

    NavHost(navController, startDestination = startDestination) {
        // Auth Screens
        composable(
            route = NavigationItem.Login.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.LOGIN.name}" })
        ) {
            LoginScreen(navController)
        }

        composable(
            route = NavigationItem.SignUp.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.SIGNUP.name}" })
        ) {
            SignUpScreen(navController)
        }

        composable(
            route = NavigationItem.Verify.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.VERIFY.name}" })
        ) {
            VerifyEmailScreen(navController)
        }

        // Bottom bar screens
        composable(
            route = NavigationItem.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.HOME.name}" })
        ) {
            HomeScreen(navController)
        }

        composable(
            route = NavigationItem.CreateCategory.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.CREATE_CATEGORY_SCREEN.name}" })
        ) {
            CreateCategoryScreen(navController)
        }

        composable(
            route = NavigationItem.AllCategory.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.ALL_CATEGORY_SCREEN.name}" })
        ) {
            AllCategoryScreen(navController)
        }

        composable(
            route = NavigationItem.CategorySetting.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.CATEGORY_SETTING_SCREEN.name}/{categoryId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            ClubCategorySettingScreen(navController = navController, categoryId = categoryId)
        }

        composable(
            route = NavigationItem.SingleCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.SINGLE_CATEGORY_SCREEN.name}/{categoryId}"
            })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            SingleCategoryScreen(categoryId = categoryId, navController = navController)
        }

        composable(
            route = NavigationItem.CreateClub.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.CREATE_CLUB_SCREEN.name}/{categoryId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            CreateClubScreen(categoryId = categoryId, userId = userId, navController = navController)
        }

        composable(
            route = NavigationItem.AllClubs.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.All_CLUBS_SCREEN.name}" })
        ) {
            AllClubScreen(navController)
        }

        composable(
            route = NavigationItem.ClubDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.CLUB_DETAIL_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            ClubDetailScreen(navController = navController, categoryId = categoryId, clubId = clubId, userId = userId)
        }

        composable(
            route = NavigationItem.ClubSetting.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.CLUB_SETTING_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            ClubSettingScreen(navController = navController, categoryId = categoryId, clubId = clubId, userId = userId)
        }

        composable(
            route = NavigationItem.EditClub.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.EDIT_CLUB_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            EditClubDetailScreen(navController = navController, categoryId = categoryId, clubId = clubId)
        }

        composable(
            route = NavigationItem.ManageClubRoles.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.MANAGE_CLUB_ROLES.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            ManageClubRolesScreen(categoryId = categoryId, clubId = clubId, currentUserId = userId, navController)
        }

        composable(
            route = NavigationItem.CreateEvent.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.CREATE_EVENT_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            CreateEventScreen(navController = navController, categoryId = categoryId, clubId = clubId, userId = userId)
        }

        composable(
            route = NavigationItem.EventDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.EVENT_DETAIL_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            EventDetailScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId)
        }

        composable(
            route = NavigationItem.EventRegistration.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.EVENT_REGISTRATION_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) {
            val args = it.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            EventRegistrationScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId, userId = userId)
        }

        composable(
            route = NavigationItem.EventSetting.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.EVENT_SETTING_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) {
            val args = it.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            EventSettingScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId)
        }

        composable(
            route = NavigationItem.AllTeamsForEvent.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.ALL_TEAMS_FOR_EVENT.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) {
            val args = it.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            AllTeamsForEventScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId)
        }

        composable(
            route = NavigationItem.TeamDetail.route,
            arguments = listOf(
                navArgument("ticketId") { type = NavType.StringType },
                navArgument("teamId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.TEAM_DETAIL_SCREEN.name}/{ticketId}/{teamId}"
            })
        ) {
            val ticketId = it.arguments?.getString("ticketId") ?: ""
            val teamId = it.arguments?.getString("teamId") ?: ""
            TeamDetailedScreen(ticketId = ticketId, teamId = teamId, navController = navController)
        }

        composable(
            route = NavigationItem.Chats.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.CHATS.name}" })
        ) {
            ChatScreen(navController)
        }

        composable(
            route = NavigationItem.Profile.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.PROFILE.name}" })
        ) {
            ProfileScreen(navController)
        }

        // User Screens
        composable(
            route = NavigationItem.UserTicket.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.User_TICKET_SCREEN.name}" })
        ) {
            UserTicketScreen(navController, userId)
        }

        composable(
            route = NavigationItem.UserEvent.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.User_EVENT_SCREEN.name}" })
        ) {
            ProfileScreen(navController)
        }

        composable(
            route = NavigationItem.UserClub.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.User_CLUB_SCREEN.name}" })
        ) {
            ProfileScreen(navController)
        }

        composable(
            route = NavigationItem.EditProfile.route,
            deepLinks = listOf(navDeepLink { uriPattern = "sitevent://app/${Screen.EDIT_PROFILE_SCREEN.name}" })
        ) {
            ProfileScreen(navController)
        }

        composable(
            route = NavigationItem.UserTicketDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("ticketId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "sitevent://app/${Screen.USER_TICKET_DETAIL_SCREEN.name}/{userId}/{ticketId}"
            })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            val ticketId = it.arguments?.getString("ticketId") ?: ""
            UserTicketDetailedScreen(userId = userId, ticketId = ticketId, navController = navController)
        }


        composable(NavigationItem.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController)
        }

        composable(NavigationItem.ReportBug.route) {
            ReportBugScreen(navController)
        }

    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val tabs = listOf(
        BottomNavigationItem("Home", Icons.Default.Home, NavigationItem.Home.route),
        BottomNavigationItem("Clubs", Icons.Default.Groups, NavigationItem.AllClubs.route),
        BottomNavigationItem("Chats", Icons.AutoMirrored.Filled.Chat, NavigationItem.Chats.route),
        BottomNavigationItem("Profile", Icons.Default.Person, NavigationItem.Profile.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        tabs.forEach { tab ->
            val selected = (tab.route == currentRoute)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                label = { Text(tab.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    selectedTextColor = Color.Blue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Blue.copy(alpha = 0.12f)
                )
            )
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

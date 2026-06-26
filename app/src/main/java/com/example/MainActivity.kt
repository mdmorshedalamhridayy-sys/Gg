package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReceiveScreen
import com.example.ui.screens.SendScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PeerDevice
import com.example.viewmodel.QuickShareViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: QuickShareViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return QuickShareViewModel(application = this@MainActivity.application) as T
                            }
                        }
                    )
                    
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSend = { navController.navigate("send") },
                                onNavigateToReceive = { navController.navigate("receive") }
                            )
                        }
                        composable("send") {
                            SendScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToTransfer = { device, file, size ->
                                    val encName = URLEncoder.encode(device.name, StandardCharsets.UTF_8.toString())
                                    val encAddr = URLEncoder.encode(device.address, StandardCharsets.UTF_8.toString())
                                    val encFile = URLEncoder.encode(file, StandardCharsets.UTF_8.toString())
                                    navController.navigate("transfer/$encName/$encAddr/$encFile/$size/true")
                                }
                            )
                        }
                        composable("receive") {
                            ReceiveScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToTransfer = { device ->
                                    val encName = URLEncoder.encode(device.name, StandardCharsets.UTF_8.toString())
                                    val encAddr = URLEncoder.encode(device.address, StandardCharsets.UTF_8.toString())
                                    val size = (50..200).random().toLong()
                                    val encFile = URLEncoder.encode("received_file.mp4", StandardCharsets.UTF_8.toString())
                                    navController.navigate("transfer/$encName/$encAddr/$encFile/$size/false")
                                }
                            )
                        }
                        composable(
                            "transfer/{name}/{addr}/{file}/{size}/{isSend}",
                            arguments = listOf(
                                navArgument("name") { type = NavType.StringType },
                                navArgument("addr") { type = NavType.StringType },
                                navArgument("file") { type = NavType.StringType },
                                navArgument("size") { type = NavType.LongType },
                                navArgument("isSend") { type = NavType.BoolType }
                            )
                        ) { backStackEntry ->
                            val name = URLDecoder.decode(backStackEntry.arguments?.getString("name") ?: "", StandardCharsets.UTF_8.toString())
                            val addr = URLDecoder.decode(backStackEntry.arguments?.getString("addr") ?: "", StandardCharsets.UTF_8.toString())
                            val file = URLDecoder.decode(backStackEntry.arguments?.getString("file") ?: "", StandardCharsets.UTF_8.toString())
                            val size = backStackEntry.arguments?.getLong("size") ?: 0L
                            val isSend = backStackEntry.arguments?.getBoolean("isSend") ?: true
                            
                            TransferScreen(
                                viewModel = viewModel,
                                device = PeerDevice(name, addr),
                                fileName = file,
                                fileSizeMb = size,
                                isSend = isSend,
                                onBack = {
                                    navController.popBackStack("home", inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

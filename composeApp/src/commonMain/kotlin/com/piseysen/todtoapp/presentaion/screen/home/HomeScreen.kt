package com.piseysen.todtoapp.presentaion.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.piseysen.todtoapp.domain.RequestState
import com.piseysen.todtoapp.domain.TaskAction
import com.piseysen.todtoapp.domain.ToDoTask
import com.piseysen.todtoapp.presentaion.components.ErrorScreen
import com.piseysen.todtoapp.presentaion.components.LoadingScreen
import com.piseysen.todtoapp.presentaion.components.TaskView
import com.piseysen.todtoapp.presentaion.screen.task.TaskScreen

class HomeScreen : Screen {
    override val key: String = "HomeScreen"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<HomeViewModel>()
        val activeTasks by viewModel.activeTasks
        val completedTasks by viewModel.completedTasks

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = "My Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(TaskScreen()) },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    DisplayTasks(
                        modifier = Modifier.fillMaxWidth(),
                        tasks = activeTasks,
                        onSelect = { selectedTask ->
                            navigator.push(TaskScreen(selectedTask))
                        },
                        onFavorite = { task, isFavorite ->
                            viewModel.setAction(TaskAction.SetFavorite(task, isFavorite))
                        },
                        onComplete = { task, completed ->
                            viewModel.setAction(TaskAction.SetCompleted(task, completed))
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    DisplayTasks(
                        modifier = Modifier.fillMaxWidth(),
                        tasks = completedTasks,
                        showActive = false,
                        onComplete = { task, completed ->
                            viewModel.setAction(TaskAction.SetCompleted(task, completed))
                        },
                        onDelete = { task ->
                            viewModel.setAction(TaskAction.Delete(task))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayTasks(
    modifier: Modifier = Modifier,
    tasks: RequestState<List<ToDoTask>>,
    showActive: Boolean = true,
    onSelect: ((ToDoTask) -> Unit)? = null,
    onFavorite: ((ToDoTask, Boolean) -> Unit)? = null,
    onComplete: (ToDoTask, Boolean) -> Unit,
    onDelete: ((ToDoTask) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    var taskToDelete: ToDoTask? by remember { mutableStateOf(null) }

    if (showDialog) {
        AlertDialog(
            title = {
                Text(
                    text = "Delete Task",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove '${taskToDelete!!.title}' task?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete?.invoke(taskToDelete!!)
                        showDialog = false
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    taskToDelete = null
                    showDialog = false
                }) {
                    Text(text = "Cancel")
                }
            },
            onDismissRequest = {
                taskToDelete = null
                showDialog = false
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        tasks.DisplayResult(
            onLoading = { LoadingScreen() },
            onError = { ErrorScreen(message = it) },
            onSuccess = { taskList ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (showActive) "Active Tasks" else "Completed Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${taskList.size} tasks",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (taskList.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = taskList,
                                key = { task -> task._id.toHexString() }
                            ) { task ->
                                TaskView(
                                    showActive = showActive,
                                    task = task,
                                    onSelect = { onSelect?.invoke(task) },
                                    onComplete = { selectedTask, completed ->
                                        onComplete(selectedTask, completed)
                                    },
                                    onFavorite = { selectedTask, favorite ->
                                        onFavorite?.invoke(selectedTask, favorite)
                                    },
                                    onDelete = { selectedTask ->
                                        taskToDelete = selectedTask
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Text(
                                text = if (showActive) "No active tasks" else "No completed tasks",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        )
    }
}
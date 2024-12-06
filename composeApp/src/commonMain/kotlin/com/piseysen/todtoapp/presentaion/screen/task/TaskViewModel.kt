package com.piseysen.todtoapp.presentaion.screen.task

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.piseysen.todtoapp.data.RealmMongoDB
import com.piseysen.todtoapp.domain.TaskAction
import com.piseysen.todtoapp.domain.ToDoTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class TaskViewModel(
    private val mongoDB: RealmMongoDB
): ScreenModel {
    fun setAction(action: TaskAction) {
        when (action) {
            is TaskAction.Add -> {
                addTask(action.task)
            }

            is TaskAction.Update -> {
                updateTask(action.task)
            }

            else -> {}
        }
    }

    private fun addTask(task: ToDoTask) {
        screenModelScope.launch(Dispatchers.IO) {
            mongoDB.addTask(task)
        }
    }

    private fun updateTask(task: ToDoTask) {
        screenModelScope.launch(Dispatchers.IO) {
            mongoDB.updateTask(task)
        }
    }
}
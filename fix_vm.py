import re

with open('app/src/main/java/com/example/ui/NexFyViewModel.kt', 'r') as f:
    text = f.read()

# Fix trashItems extra stateIn
text = text.replace(
"""    val trashItems: StateFlow<List<com.example.data.model.TrashItem>> = trashDao.getAllTrashItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )""",
"""    val trashItems: StateFlow<List<com.example.data.model.TrashItem>> = trashDao.getAllTrashItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )"""
)

with open('app/src/main/java/com/example/ui/NexFyViewModel.kt', 'w') as f:
    f.write(text)

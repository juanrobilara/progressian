package com.jurobil.progressian.ui.screens.feedScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.jurobil.progressian.domain.model.Comment
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.model.PostType
import com.jurobil.progressian.ui.screens.feedScreen.components.CreatePostDialog
import com.jurobil.progressian.ui.screens.feedScreen.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()


    var showCreateDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(state.message) {
        if (state.message != null) {
            snackbarHostState.showSnackbar(state.message!!)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Publicar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.posts, key = { post -> post.id }) { post ->
                RpgPostCard(
                    post = post,
                    currentUserId = viewModel.currentUserId,
                    onLikeClick = { viewModel.toggleLike(post) },
                    onCommentClick = {
                        selectedPostIdForComments = post.id
                        showCommentsSheet = true
                    },
                    onCloneClick = { habit ->
                        viewModel.cloneHabit(habit)
                    },
                    onEditClick = { postToEdit = post },
                    onDeleteClick = { postToDelete = post }
                )
            }
        }
    }


    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onPublish = {
                viewModel.createPost(it)
                showCreateDialog = false
            }
        )
    }

    if (postToEdit != null) {
        EditPostDialog(
            post = postToEdit!!,
            onDismiss = { postToEdit = null },
            onConfirm = { newContent ->
                viewModel.updatePost(postToEdit!!, newContent)
                postToEdit = null
            }
        )
    }

    if (postToDelete != null) {
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Esta acción no se puede deshacer. El post desaparecerá del reino para siempre.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(postToDelete!!)
                        postToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }


    if (showCommentsSheet && selectedPostIdForComments != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showCommentsSheet = false
                selectedPostIdForComments = null
            },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets.ime }
        ) {
            CommentsSection(
                postId = selectedPostIdForComments!!,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun RpgPostCard(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onCloneClick: (Habit) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit
) {
    var showFullDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.authorPhotoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.authorPhotoUrl)
                            .crossfade(true)
                            .size(100, 100)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.authorName.take(1), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aventurero",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.weight(1f))

                if (post.authorId == currentUserId) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    showMenu = false
                                    onEditClick(post)
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(post)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (post.type == PostType.HABIT_PLAN) 2 else 4,
                overflow = TextOverflow.Ellipsis
            )

            if (post.content.length > 150) {
                TextButton(
                    onClick = { showFullDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Leer pergamino completo...", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (post.type == PostType.HABIT_PLAN && post.sharedHabit != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📜 Pergamino de Hábito", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(post.sharedHabit.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${post.sharedHabit.missions.size} misiones • ${post.sharedHabit.totalXpReward} XP", style = MaterialTheme.typography.bodySmall)

                        if (post.authorId != currentUserId) {
                            Button(
                                onClick = { onCloneClick(post.sharedHabit) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Copiar a mis Hábitos")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByCurrentUser) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${post.likeCount}",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.width(24.dp))


                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comentar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${post.commentCount}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }


    if (showFullDialog) {
        AlertDialog(
            onDismissRequest = { showFullDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Crónica de ${post.authorName}", color = MaterialTheme.colorScheme.primary)
            },
            text = {
                Column {
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showFullDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSection(
    postId: String,
    viewModel: FeedViewModel
) {
    val comments by viewModel.getComments(postId).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text("Comentarios", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sé el primero en comentar...",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = comments,
                    key = { it.id }
                ) { comment ->



                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { swipeValue ->
                            if (swipeValue == SwipeToDismissBoxValue.StartToEnd) {

                                text = "*${comment.userName}* "
                                focusRequester.requestFocus()
                                return@rememberSwipeToDismissBoxState false
                            }
                            false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {

                            val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, shape = MaterialTheme.shapes.small)
                                    .padding(start = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = "Responder",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        content = {

                            CommentRow(comment)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Escribe algo...") },
                singleLine = true,
                shape = CircleShape
            )
            IconButton(onClick = {
                if (text.isNotBlank()) {
                    viewModel.addComment(postId, text)
                    text = ""
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CommentRow(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.Top
    ) {

        if (comment.userPhotoUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comment.userPhotoUrl)
                    .crossfade(true)
                    .size(100, 100)
                    .transformations(CircleCropTransformation())
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.userName.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            val styledText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                ) {
                    append("${comment.userName}\n")
                }

                val words = comment.content.split(" ")
                words.forEachIndexed { index, word ->

                    if (word.startsWith("*") && word.endsWith("*") && word.length > 2) {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(word.removePrefix("*").removeSuffix("*"))
                        }
                    } else {

                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                            append(word)
                        }
                    }
                    if (index < words.size - 1) append(" ")
                }
            }

            Text(
                text = styledText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
fun EditPostDialog(
    post: Post,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(post.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Crónica") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
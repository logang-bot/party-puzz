package com.restrusher.partypuzl.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.CreateCustomPackScreen
import com.restrusher.partypuzl.ui.views.customPacks.editor.ui.CustomPackEditorScreen
import com.restrusher.partypuzl.ui.views.customPacks.entry.ui.CreateCustomEntryScreen
import com.restrusher.partypuzl.ui.views.customPacks.list.ui.CustomPacksScreen

/**
 * The custom-pack authoring flow: manager → pack shell → contents → one entry.
 *
 * Lives in its own file so `HomeNavigation` doesn't grow another sixty lines. Nothing here creates
 * a ViewModel or runs a `LaunchedEffect` — the screens own both.
 */
fun NavGraphBuilder.customPacksGraph(
    navController: NavHostController,
    setAppBarTitle: (String) -> Unit
) {
    composable<CustomPacksRoute> {
        CustomPacksScreen(
            setAppBarTitle = setAppBarTitle,
            onCreatePack = { navController.navigate(CreateCustomPackRoute()) },
            onOpenPack = { packId -> navController.navigate(CustomPackEditorRoute(packId)) },
            onEditPack = { packId -> navController.navigate(CreateCustomPackRoute(packId)) },
            modifier = Modifier.fillMaxSize()
        )
    }

    composable<CreateCustomPackRoute> { entry ->
        val packId = entry.toRoute<CreateCustomPackRoute>().packId
        CreateCustomPackScreen(
            packId = packId,
            setAppBarTitle = setAppBarTitle,
            onSaved = { savedId ->
                // Creating drops the user straight into the empty editor, so the next thing they
                // see is "add your first entry". Editing just returns to where they came from.
                if (packId == null) {
                    navController.navigate(CustomPackEditorRoute(savedId)) {
                        popUpTo<CreateCustomPackRoute> { inclusive = true }
                    }
                } else {
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    composable<CustomPackEditorRoute> { entry ->
        val packId = entry.toRoute<CustomPackEditorRoute>().packId
        CustomPackEditorScreen(
            packId = packId,
            setAppBarTitle = setAppBarTitle,
            onAddEntry = { navController.navigate(CreateCustomEntryRoute(packId)) },
            onEditEntry = { entryId ->
                navController.navigate(CreateCustomEntryRoute(packId, entryId))
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    composable<CreateCustomEntryRoute> { entry ->
        val args = entry.toRoute<CreateCustomEntryRoute>()
        CreateCustomEntryScreen(
            packId = args.packId,
            entryId = args.entryId,
            setAppBarTitle = setAppBarTitle,
            onSaved = { navController.popBackStack() },
            modifier = Modifier.fillMaxSize()
        )
    }
}

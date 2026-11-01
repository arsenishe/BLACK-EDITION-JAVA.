package com.ashmeet.hyperlauncher.screens.home

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashmeet.hyperbackend.authenticator.accounts.Accounts
import com.ashmeet.hyperbackend.instances.DisplayInstance
import com.ashmeet.hyperbackend.instances.InstanceIconProvider
import com.ashmeet.hyperbackend.instances.Instances
import com.ashmeet.hyperlauncher.extra.ExtraConstants
import com.ashmeet.hyperlauncher.extra.ExtraCore
import com.ashmeet.hyperlauncher.extra.ExtraListener
import com.ashmeet.hyperlauncher.screens.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.settings.preferences.LauncherPreferences
import com.ashmeet.hyperlauncher.screens.settings.preferences.SettingsActionItem
import com.ashmeet.hyperlauncher.theme.PojavTheme
import com.ashmeet.hyperlauncher.utils.SkinUtils
import com.ashmeet.hyperlauncher.utils.drawable.rememberDrawablePainter
import com.ashmeet.hyperlauncher.utils.translation.translatedText
import net.ashmeet.hyperlauncher.R

@SuppressLint("LocalContextResourcesRead")
@Composable
fun MainMenuFragmentCompose(
    onAboutClick: () -> Unit,
    onSocialMediaClick: () -> Unit,
    onCustomControlsClick: () -> Unit,
    onInstallJarClick: () -> Unit,
    onShareLogsClick: () -> Unit,
    onOpenFilesClick: () -> Unit,
    onEditProfileClick: (DisplayInstance?) -> Unit,
    onPlayClick: () -> Unit,
    onVersionSpinnerClick: () -> Unit,
    onAccountManagerClick: () -> Unit,
    onContentInstallerClick: () -> Unit = {},
    onInstanceDirectoryClick: () -> Unit = {},
    onRecordingsGalleryClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var launcherBgPath by remember {
        mutableStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_PATH)
    }

    val hasBackground = launcherBgPath != null
    val backgroundTransparency = if (hasBackground) 0.5f else 1f

    var hideActionButtons by remember {
        mutableStateOf(LauncherPreferences.PREF_HIDE_SIDEBAR)
    }

    var selectedInstance by remember {
        mutableStateOf(
            if (isPreview) null
            else try {
                Instances.loadSelectedInstance()
            } catch (_: Exception) {
                null
            }
        )
    }

    if (!isPreview) {
        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "hide_sidebar") {
                    hideActionButtons =
                        LauncherPreferences.prefs.getBoolean("hide_sidebar", false)
                }

                if (key == "launcher_background_path") {
                    launcherBgPath =
                        LauncherPreferences.prefs.getString("launcher_background_path", null)
                }

                if (key == LauncherPreferences.PREF_KEY_CURRENT_INSTANCE) {
                    selectedInstance = try {
                        Instances.loadSelectedInstance()
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            LauncherPreferences.prefs.registerOnSharedPreferenceChangeListener(listener)

            onDispose {
                LauncherPreferences.prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    SideEffect {
        if (!isPreview) {
            val instance = try {
                Instances.loadSelectedInstance()
            } catch (_: Exception) {
                null
            }

            if (selectedInstance != instance) {
                selectedInstance = instance
            }
        }
    }

    var currentAccount by remember {
        mutableStateOf(if (isPreview) null else Accounts.getCurrent())
    }

    DisposableEffect(Unit) {
        if (isPreview) return@DisposableEffect onDispose {}

        val accountListener = ExtraListener<Any> { _, _ ->
            currentAccount = Accounts.getCurrent()
            false
        }

        ExtraCore.addExtraListener(
            ExtraConstants.REFRESH_ACCOUNT_SPINNER,
            accountListener
        )

        onDispose {
            ExtraCore.removeExtraListenerFromValue(
                ExtraConstants.REFRESH_ACCOUNT_SPINNER,
                accountListener
            )
        }
    }

    val skinHead by SkinUtils.rememberSkinHead2D(currentAccount)

    val instanceIcon = remember(selectedInstance) {
        if (!isPreview && selectedInstance != null) {
            InstanceIconProvider.fetchIcon(context.resources, selectedInstance!!)
        } else {
            null
        }
    }

    val headInteractionSource = remember { MutableInteractionSource() }
    val isHeadPressed by headInteractionSource.collectIsPressedAsState()

    val headScale by animateFloatAsState(
        targetValue = if (isHeadPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "headScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.dragon_background),
                contentScale = ContentScale.Crop
            ),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isPortrait = maxHeight > maxWidth

            if (isPortrait) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ActionCardsContent(
                            hideActionButtons = hideActionButtons,
                            isPortrait = isPortrait,
                            onAboutClick = onAboutClick,
                            onSocialMediaClick = onSocialMediaClick,
                            onCustomControlsClick = onCustomControlsClick,
                            onContentInstallerClick = onContentInstallerClick,
                            onInstanceDirectoryClick = onInstanceDirectoryClick,
                            onRecordingsGalleryClick = onRecordingsGalleryClick,
                            onInstallJarClick = onInstallJarClick,
                            onShareLogsClick = onShareLogsClick,
                            onOpenFilesClick = onOpenFilesClick
                        )
                    }

                    SidebarCardContent(
                        modifier = Modifier.fillMaxWidth(),
                        hasBackground = hasBackground,
                        backgroundTransparency = backgroundTransparency,
                        headScale = headScale,
                        headInteractionSource = headInteractionSource,
                        currentAccount = currentAccount,
                        skinHead = skinHead,
                        instanceIcon = instanceIcon,
                        selectedInstance = selectedInstance,
                        onAccountManagerClick = onAccountManagerClick,
                        onVersionSpinnerClick = onVersionSpinnerClick,
                        onEditProfileClick = onEditProfileClick,
                        onPlayClick = onPlayClick,
                        showHead = false
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val rightSidebarWidth = 260.dp

                        if (!hideActionButtons) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ActionCardsContent(
                                    hideActionButtons = hideActionButtons,
                                    isPortrait = isPortrait,
                                    onAboutClick = onAboutClick,
                                    onSocialMediaClick = onSocialMediaClick,
                                    onCustomControlsClick = onCustomControlsClick,
                                    onContentInstallerClick = onContentInstallerClick,
                                    onInstanceDirectoryClick = onInstanceDirectoryClick,
                                    onRecordingsGalleryClick = onRecordingsGalleryClick,
                                    onInstallJarClick = onInstallJarClick,
                                    onShareLogsClick = onShareLogsClick,
                                    onOpenFilesClick = onOpenFilesClick
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        SidebarCardContent(
                            modifier = Modifier
                                .width(rightSidebarWidth)
                                .fillMaxHeight(),
                            hasBackground = hasBackground,
                            backgroundTransparency = backgroundTransparency,
                            headScale = headScale,
                            headInteractionSource = headInteractionSource,
                            currentAccount = currentAccount,
                            skinHead = skinHead,
                            instanceIcon = instanceIcon,
                            selectedInstance = selectedInstance,
                            onAccountManagerClick = onAccountManagerClick,
                            onVersionSpinnerClick = onVersionSpinnerClick,
                            onEditProfileClick = onEditProfileClick,
                            onPlayClick = onPlayClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCardsContent(
    hideActionButtons: Boolean,
    isPortrait: Boolean,
    onAboutClick: () -> Unit,
    onSocialMediaClick: () -> Unit,
    onCustomControlsClick: () -> Unit,
    onContentInstallerClick: () -> Unit,
    onInstanceDirectoryClick: () -> Unit,
    onRecordingsGalleryClick: () -> Unit,
    onInstallJarClick: () -> Unit,
    onShareLogsClick: () -> Unit,
    onOpenFilesClick: () -> Unit
) {
    if (!hideActionButtons) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ActionCard(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                title = translatedText(stringResource(R.string.home_about)),
                icon = Icons.Rounded.Info,
                position = CardPosition.TOP,
                onClick = onAboutClick
            )

            ActionCard(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                title = translatedText(stringResource(id = R.string.mcl_button_social_media)),
                icon = Icons.Rounded.Share,
                position = CardPosition.MIDDLE,
                onClick = onSocialMediaClick
            )

            ActionCard(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                title = translatedText(stringResource(id = R.string.mcl_option_customcontrol)),
                icon = Icons.Rounded.Build,
                position = CardPosition.MIDDLE,
                onClick = onCustomControlsClick
            )

            if (isPortrait) {
                ActionCard(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    title = translatedText(stringResource(R.string.home_content_installer)),
                    icon = Icons.Rounded.Download,
                    position = CardPosition.MIDDLE,
                    onClick = onContentInstallerClick
                )

                ActionCard(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    title = translatedText(stringResource(R.string.home_instance_directory)),
                    icon = Icons.Rounded.Folder,
                    position = CardPosition.MIDDLE,
                    onClick = onInstanceDirectoryClick
                )

                ActionCard(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    title = translatedText(stringResource(R.string.home_recordings_gallery)),
                    icon = Icons.Rounded.Videocam,
                    position = CardPosition.MIDDLE,
                    onClick = onRecordingsGalleryClick
                )
            }

            ActionCard(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                title = translatedText(stringResource(id = R.string.main_install_jar_file)),
                icon = Icons.Filled.Add,
                position = CardPosition.MIDDLE,
                onClick = onInstallJarClick
            )

            ActionCard(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                title = translatedText(stringResource(id = R.string.main_share_logs)),
                icon = Icons.AutoMirrored.Rounded.Send,
                position = CardPosition.MIDDLE,
                onClick = onShareLogsClick
            )

            ActionCard(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                title = translatedText(stringResource(id = R.string.mcl_button_open_directory)),
                icon = Icons.Rounded.Search,
                position = CardPosition.BOTTOM,
                onClick = onOpenFilesClick
            )
        }
    }
}

@Composable
private fun SidebarCardContent(
    modifier: Modifier = Modifier,
    hasBackground: Boolean,
    backgroundTransparency: Float,
    headScale: Float,
    headInteractionSource: MutableInteractionSource,
    currentAccount: com.ashmeet.hyperbackend.authenticator.accounts.Account?,
    skinHead: android.graphics.Bitmap?,
    instanceIcon: android.graphics.drawable.Drawable?,
    selectedInstance: DisplayInstance?,
    onAccountManagerClick: () -> Unit,
    onVersionSpinnerClick: () -> Unit,
    onEditProfileClick: (DisplayInstance?) -> Unit,
    onPlayClick: () -> Unit,
    showHead: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = (if (showHead) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 110.dp)
                    .scale(headScale)
                    .clickable(
                        interactionSource = headInteractionSource,
                        indication = null,
                        onClick = onAccountManagerClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.black_edition_logo),
                    contentDescription = "BLACK EDITION JAVA",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0x55301850),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 6.dp,
                        top = 6.dp,
                        bottom = 6.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onVersionSpinnerClick)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (instanceIcon != null) {
                                Image(
                                    painter = rememberDrawablePainter(instanceIcon),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFFBB86FC)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val name = selectedInstance?.name

                            val instanceDisplayName = if (selectedInstance == null) {
                                translatedText(stringResource(id = R.string.no_instance))
                            } else if (name.isNullOrBlank()) {
                                translatedText(stringResource(R.string.home_unnamed))
                            } else {
                                name
                            }

                            Text(
                                text = instanceDisplayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp,
                                color = Color.White
                            )

                            Text(
                                text = selectedInstance?.versionId
                                    ?: translatedText(
                                        stringResource(id = R.string.version_select_hint)
                                    ),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                color = Color(0xFFD0B5FF)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onEditProfileClick(selectedInstance) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = translatedText(
                                stringResource(R.string.home_edit_profile)
                            ),
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFD0B5FF)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF160B24),
                    contentColor = Color.White,
                    border = BorderStroke(
                        width = 2.dp,
                        color = Color(0xFFBD55FF)
                    ),
                    shadowElevation = 14.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PLAY",
                            color = Color(0xFFE6C5FF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    position: CardPosition = CardPosition.SINGLE,
    onClick: () -> Unit
) {
    SettingsCard(
        modifier = modifier,
        position = position,
        useSurface = true
    ) {
        SettingsActionItem(
            title = title,
            icon = icon,
            onClick = onClick
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=400dp,dpi=420"
)
@Composable
fun MainMenuRevampPreview() {
    PojavTheme {
        MainMenuFragmentCompose(
            onAboutClick = {},
            onSocialMediaClick = {},
            onCustomControlsClick = {},
            onInstallJarClick = {},
            onShareLogsClick = {},
            onOpenFilesClick = {},
            onEditProfileClick = {},
            onPlayClick = {},
            onVersionSpinnerClick = {},
            onAccountManagerClick = {}
        )
    }
}

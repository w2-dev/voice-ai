package com.tapbi.spark.voiceai.ui.main.language

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.common.models.Language
import com.tapbi.spark.ui_common_sdk.components.ContentState
import com.tapbi.spark.ui_common_sdk.components.loadings.CircularLoadingScreen
import com.tapbi.spark.ui_common_sdk.components.ui.LanguageHeader
import com.tapbi.spark.ui_common_sdk.extension.showToast
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_625
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_75
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2_25
import com.tapbi.spark.ui_common_sdk.foundation.grid_x3_75
import com.tapbi.spark.ui_common_sdk.foundation.round10

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel
) {
    val resource = LocalResources.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = modifier) {
        LanguageHeader(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.languages),
            iconBack = AppTheme.drawable.icBack,
            iconFinish = AppTheme.drawable.icDone,
            isHideBack = uiState.isFromSplash,
            isHideAction = uiState.selectedLocale.isEmpty(),
            actionBack = {

            },
            actionFinish = {

                viewModel.setLanguage(uiState.selectedLocale)
                if (uiState.isFromSplash) {
                    context.showToast(resource.getString(R.string.success))
                    viewModel.finishSetLanguage(true)
                }
            }
        )

        when (uiState.contentState) {
            ContentState.EMPTY, ContentState.LOADING -> {
                CircularLoadingScreen(
                    color = AppTheme.color.colorFFD755,
                    modifier = Modifier.fillMaxSize()
                )

            }

            ContentState.LOADED -> {
                ListLanguages(
                    modifier = Modifier
                        .animateContentSize()
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = grid_x2)
                        .padding(bottom = grid_x2, top = grid_x0_5),
                    listLanguage = uiState.data,
                    currentLocale = uiState.selectedLocale,
                    onLanguageSelected = { code, pos ->
                        viewModel.updateSelectedLocale(code)
                    }
                )
            }

            ContentState.ERROR -> {}
        }

    }

}

@Composable
fun ListLanguages(
    modifier: Modifier = Modifier,
    currentLocale: String,
    listLanguage: List<Language>,
    onLanguageSelected: (String, Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(
            items = listLanguage,
            key = { _, item -> item.codeLocale },
            contentType = { _, _ -> "language_item" }
        ) { index, language ->
            LanguageItem(
                modifier = Modifier
                    .padding(vertical = grid_x0_75)
                    .clip(round10)
                    .background(color = AppTheme.color.colorWhite)
                    .fillMaxWidth(),
                language = language,
                isSelected = currentLocale == language.codeLocale,
                onClick = {
                    onLanguageSelected(language.codeLocale, index)
                }
            )
        }
    }
}

@Composable
private fun LanguageItem(
    modifier: Modifier = Modifier,
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            modifier = Modifier.padding(grid_x2_25),
            painter = painterResource(id = language.imNationalFlag),
            contentDescription = null,
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = grid_x3_75, horizontal = grid_x0_625),
            text = language.nameLanguage,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.color.colorBlack
        )

        RadioButton(
            selected = isSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppTheme.color.colorFFD755,
                unselectedColor = AppTheme.color.color5A5D5D
            ),
            onClick = onClick,
            modifier = Modifier.padding(grid_x2_25)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {

}

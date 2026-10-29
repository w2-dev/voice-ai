package com.tapbi.spark.voiceai.ui.main.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.ui_common_sdk.components.ContentState
import com.tapbi.spark.ui_common_sdk.components.loadings.CircularLoadingScreen
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.common.models.Language

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
    ) {
        // Top Header (1 & 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Choose Language",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        val localeToSave = uiState.selectedLocale.ifBlank { Constant.LANGUAGE_EN }
                        viewModel.setLanguage(localeToSave)
                        viewModel.finishSetLanguage(uiState.isFromSplash)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = AppTheme.drawable.icDone),
                    contentDescription = "Confirm",
                    tint = Color(0xFF29B6F6),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        when (uiState.contentState) {
            ContentState.EMPTY, ContentState.LOADING -> {
                CircularLoadingScreen(
                    color = Color(0xFF29B6F6),
                    modifier = Modifier.fillMaxSize()
                )
            }

            ContentState.LOADED -> {
                ListLanguages(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    listLanguage = uiState.data,
                    currentLocale = uiState.selectedLocale,
                    onLanguageSelected = { code, _ ->
                        viewModel.updateSelectedLocale(code)
                    }
                )
            }

            ContentState.ERROR -> {}
        }
    }
}

@Composable
private fun ListLanguages(
    modifier: Modifier = Modifier,
    currentLocale: String,
    listLanguage: List<Language>,
    onLanguageSelected: (String, Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        itemsIndexed(
            items = listLanguage,
            key = { _, item -> item.codeLocale },
            contentType = { _, _ -> "language_item" }
        ) { index, language ->
            LanguageItem(
                language = language,
                isSelected = currentLocale == language.codeLocale || (currentLocale.isBlank() && language.codeLocale == Constant.LANGUAGE_EN),
                onClick = {
                    onLanguageSelected(language.codeLocale, index)
                }
            )
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF29B6F6) else Color(0xFFEEEEEE),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = language.imNationalFlag),
                        contentDescription = language.nameLanguage,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = language.nameLanguage,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            }

            LanguageRadioIndicator(isSelected = isSelected)
        }
    }
}

@Composable
private fun LanguageRadioIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF29B6F6) else Color(0xFFE0E0E0),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF29B6F6), CircleShape)
            )
        }
    }
}

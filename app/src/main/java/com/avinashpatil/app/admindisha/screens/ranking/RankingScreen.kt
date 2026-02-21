package com.avinashpatil.app.admindisha.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.avinashpatil.app.admindisha.data.ranking.RankingItem
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var animateBackButton by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (animateBackButton) -50f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    val rankings = remember {
        listOf(
            RankingItem(1, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "AVINASH RAJENDRA PATIL", 16847),
            RankingItem(2, "https://storage.googleapis.com/a1aa/image/dkfNX5sNdpxJMZ_cbjsMqUcDNxd2VNAsG6SivUAsFyk.jpg", "PRATHMESH DATTATRAY PATIL THE KING OF SADOLI", 15902),
            RankingItem(3, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "ANIRUDDHA RAJENDRA PATIL", 15000),
            RankingItem(4, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "AVINASH RAJENDRA PATIL", 16847),
            RankingItem(5, "https://storage.googleapis.com/a1aa/image/dkfNX5sNdpxJMZ_cbjsMqUcDNxd2VNAsG6SivUAsFyk.jpg", "PRATHMESH DATTATRAY PATIL THE KING OF SADOLI", 15902),
            RankingItem(6, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "ANIRUDDHA RAJENDRA PATIL", 15000),
            RankingItem(7, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "AVINASH RAJENDRA PATIL", 16847),
            RankingItem(8, "https://storage.googleapis.com/a1aa/image/dkfNX5sNdpxJMZ_cbjsMqUcDNxd2VNAsG6SivUAsFyk.jpg", "PRATHMESH DATTATRAY PATIL THE KING OF SADOLI", 15902),
            RankingItem(9, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "ANIRUDDHA RAJENDRA PATIL", 15000),
            RankingItem(10, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "AVINASH RAJENDRA PATIL", 16847),
            RankingItem(11, "https://storage.googleapis.com/a1aa/image/dkfNX5sNdpxJMZ_cbjsMqUcDNxd2VNAsG6SivUAsFyk.jpg", "PRATHMESH DATTATRAY PATIL THE KING OF SADOLI", 15902),
            RankingItem(12, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "ANIRUDDHA RAJENDRA PATIL", 15000),
            RankingItem(13, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "AVINASH RAJENDRA PATIL", 16847),
            RankingItem(14, "https://storage.googleapis.com/a1aa/image/dkfNX5sNdpxJMZ_cbjsMqUcDNxd2VNAsG6SivUAsFyk.jpg", "PRATHMESH DATTATRAY PATIL THE KING OF SADOLI", 15902),
            RankingItem(15, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "ANIRUDDHA RAJENDRA PATIL", 15000),
            RankingItem(16, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "AVINASH RAJENDRA PATIL", 16847),
            RankingItem(17, "https://storage.googleapis.com/a1aa/image/dkfNX5sNdpxJMZ_cbjsMqUcDNxd2VNAsG6SivUAsFyk.jpg", "PRATHMESH DATTATRAY PATIL THE KING OF SADOLI", 15902),
            RankingItem(18, "https://storage.googleapis.com/a1aa/image/5pDz2nMCiRSBnJWlBiSNrESVHVPf5yRm-I5ASKFbKdI.jpg", "ANIRUDDHA RAJENDRA PATIL", 15000),
        )
    }
    val gradients = listOf(
        Brush.horizontalGradient(colors = listOf(Color(0xFFE9D5FF), Color(0xFFFBCFE8))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFBAE6FD), Color(0xFFCFFAFE))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFD1FAE5), Color(0xFFE0F2FE))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFFEE2E2), Color(0xFFFFE4E6))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFFEF3C7), Color(0xFFFFE4E6))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFDBEAFE), Color(0xFFE0E7FF))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFECFCCB), Color(0xFFD1FAE5))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFFCE7F3), Color(0xFFE9D5FF))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFE0E7FF), Color(0xFFDBEAFE))),
        Brush.horizontalGradient(colors = listOf(Color(0xFFFFE4E6), Color(0xFFFEE2E2)))
    )
    val suggestions = if (searchQuery.isNotEmpty()) {
        rankings.filter { it.name.contains(searchQuery, ignoreCase = true) }.take(3)
    } else {
        emptyList()
    }
    val filteredRankings = rankings.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6B7280), Color(0xFF4B5563))
                        )
                    )
                    .shadow(8.dp, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            animateBackButton = true
                            navController.navigateUp()
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .graphicsLayer { translationX = animatedOffset }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "LEADERBOARD",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Search Bar inside the Card
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { active = false },
                        active = active,
                        onActiveChange = { active = it },
                        placeholder = { Text("Search by student name...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = Color.White,
                            dividerColor = Color.Transparent
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            suggestions.forEach { ranking ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = ranking.name
                                            active = false
                                        }
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ranking.name,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFCC33), RoundedCornerShape(5.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rank", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Name", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Score", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(filteredRankings) { ranking ->
                            RankingListItem(
                                ranking = ranking,
                                gradient = gradients[ranking.rank.minus(1) % gradients.size]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RankingListItem(ranking: RankingItem, gradient: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(gradient, RoundedCornerShape(30.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%02d", ranking.rank),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            model = ranking.imageUrl,
            contentDescription = "Profile image of ${ranking.name}",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = ranking.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        Text(
            text = "${ranking.score}",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFFF9933),
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RankingScreenPreview() {
    val navController = rememberNavController()
    RankingScreen(navController = navController)
}
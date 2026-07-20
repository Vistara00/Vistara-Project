package com.example.vistaraapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.entities_dataclass.NotificationItem

@Composable
fun BookingInboxEntry(
    unreadCount: Int,
    latestMessage: String,
    latestTimestamp: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1C1C1E)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    val badgeBgColor = Color(0xFF029602) // PrimaryGreen
    val badgeTextColor = Color.White
    val avatarBg = if (isDark) Color(0xFF1B3B2B) else Color(0xFFE8F5E9) // soft green tint
    val avatarIconColor = Color(0xFF029602) // PrimaryGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Circle (48dp standard)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = avatarBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Safari Bookings",
                tint = avatarIconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content (Title + Preview)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Safari Bookings",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = latestMessage,
                fontSize = 14.sp,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time and Badge Column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = latestTimestamp,
                fontSize = 12.sp,
                color = if (unreadCount > 0) badgeBgColor else secondaryTextColor,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = badgeBgColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun BroadcastInboxEntry(
    unreadCount: Int,
    latestMessage: String,
    latestTimestamp: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1C1C1E)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    val badgeBgColor = Color(0xFFD32F2F) // ErrorRed
    val badgeTextColor = Color.White
    val avatarBg = if (isDark) Color(0xFF4C1D1D) else Color(0xFFFFEBEE) // soft red tint
    val avatarIconColor = Color(0xFFD32F2F) // ErrorRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Circle (48dp standard)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = avatarBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Broadcast Alerts",
                tint = avatarIconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content (Title + Preview)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Broadcast Alerts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = latestMessage,
                fontSize = 14.sp,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time and Badge Column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = latestTimestamp,
                fontSize = 12.sp,
                color = if (unreadCount > 0) badgeBgColor else secondaryTextColor,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = badgeBgColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun OtherNotificationRow(
    notification: NotificationItem,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1C1C1E)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    val badgeBgColor = Color(0xFF029602) // PrimaryGreen for general notification badge
    val badgeTextColor = Color.White
    val avatarBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7) // soft grey tint
    val avatarIconColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Circle with initial letter (48dp standard)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = avatarBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = notification.type.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = avatarIconColor,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time and Badge Column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = notification.timestamp,
                fontSize = 12.sp,
                color = if (isUnread) badgeBgColor else secondaryTextColor,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = badgeBgColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        color = textColor
    )
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1C1C1E)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    val avatarBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7) // soft grey tint
    val avatarIconColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with prefix letter (48dp standard)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = avatarBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.type.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = avatarIconColor,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Timestamp (Top Right style)
            Text(
                text = notification.timestamp,
                fontSize = 12.sp,
                color = secondaryTextColor
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
    }
}

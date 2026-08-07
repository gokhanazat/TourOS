package com.mgacreative.touros.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.recommendation.TourRecommendation
import com.mgacreative.touros.domain.model.segmentation.CustomerSegment

@Composable
fun SalesAgentRecommendationsPanel(
    customerSegment: CustomerSegment?,
    recommendations: List<TourRecommendation>,
    onPitchTourToCustomer: (TourRecommendation) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Satış Temsilcisi AI Öneri Paneli 🎯",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (customerSegment != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Segment: ${customerSegment.segmentTier.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sadakat Puanı: ${customerSegment.loyaltyPoints} PTS",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = customerSegment.customerNotes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Müşteriye Önerilebilecek Çapraz Satış Turları:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            recommendations.take(2).forEach { tour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = tour.tourName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "€${tour.price} • %${tour.matchScore.toInt()} Uyum", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { onPitchTourToCustomer(tour) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Teklif Et", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

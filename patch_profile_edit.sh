sed -i '/if (baseline != null) {/,/^[[:space:]]*}$/c\
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {\
                    Text("Measurement History", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = titleColor)\
                    TextButton(onClick = { onNavigateToEditBaseline(child.studyId) }) {\
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))\
                        Spacer(modifier = Modifier.width(4.dp))\
                        Text("Edit Baseline")\
                    }\
                }\
                if (baseline != null) {\
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {\
                        Column(modifier = Modifier.padding(12.dp)) {\
                            Text("Baseline Enrolment: ${baseline.weightEnrolment}kg", fontWeight = FontWeight.Medium)\
                            Text("Baseline 12-wk: ${baseline.weight12Weeks}kg -> ${baseline.programmeOutcome}", color = subtitleColor)\
                        }\
                    }\
                }' app/src/main/java/com/example/ui/ChildProfileScreen.kt

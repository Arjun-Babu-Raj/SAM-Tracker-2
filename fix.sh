sed -i 's/val formState by viewModel.formState.collectAsState()/val formState by viewModel.baselineState.collectAsState()/' app/src/main/java/com/example/ui/BaselineScreen.kt
sed -i 's/viewModel.resetFormState()/viewModel.resetBaselineState()/' app/src/main/java/com/example/ui/BaselineScreen.kt
sed -i 's/val formState by viewModel.followupFormState.collectAsState()/val formState by viewModel.followupState.collectAsState()/' app/src/main/java/com/example/ui/FollowupScreen.kt
sed -i 's/QuestionBank.allBaselineFields.map { it.id }/QuestionBank.baselineSections.flatMap { it.fields }.map { it.id }/g' app/src/main/java/com/example/ui/MainViewModel.kt

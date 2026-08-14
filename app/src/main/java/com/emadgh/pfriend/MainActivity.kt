package com.emadgh.pfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.emadgh.pfriend.ui.PFriendApp
import com.emadgh.pfriend.ui.theme.PFriendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PFriendTheme { PFriendApp() } }
    }
}

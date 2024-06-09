package com.github.aakumykov.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val dateTimeString: String
    get() = SimpleDateFormat("hhч mmм ssс, dd.MM.yy", Locale.getDefault()).format(Date())
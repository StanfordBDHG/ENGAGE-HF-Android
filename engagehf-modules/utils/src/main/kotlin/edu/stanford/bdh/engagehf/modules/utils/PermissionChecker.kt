package edu.stanford.bdh.engagehf.modules.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Handler class for checking permissions.
 *
 * @property context The application context.
 */
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Checks if the specified permission is granted.
     *
     * @param permission The permission to be checked.
     * @return true if the permission is granted, false otherwise.
     */
    fun isPermissionGranted(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

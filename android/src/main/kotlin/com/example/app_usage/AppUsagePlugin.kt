package com.example.app_usage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.util.Log
import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.app.AppOpsManager

/** AppUsagePlugin */
public class AppUsagePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private val methodChannelName = "app_usage.methodChannel"
    private lateinit var activity: Activity

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, methodChannelName)
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.applicationContext;
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        /// Verify that the correct method was called
        if (call.method == "getAppUsage") {
            // Log.d("kttest","getAppUsage Called!");
            // Parse parameters, i.e. start- and end-date
            val startTime: Long? = call.argument("start")
            val endTime: Long? = call.argument("end")
            val packageName: String? = call.argument("packageName")
            var res: MutableList< Map<String, String> > = getAppUsage(startTime!!, endTime!!, packageName)
            result.success(res)
        }

        else if (call.method == "getAppUsagePermission") {
            getAppUsagePermission()
        }
        /// If an incorrect method was called, throw an error
        else {
            result.notImplemented()
        }
    }

    fun getAppUsage(@NonNull startTime: Long, @NonNull endTime: Long, packageName: String? = null) : MutableList< Map<String, String> > {
      
        /// Query the Usage API
        var usageStatsManager : UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var usage : UsageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var events: MutableList< Map<String, String> > = mutableListOf()


        // Log.d("kttest","usage in kt")
        // Log.d("usage", usage.hasNextEvent().toString())
        // Log.d("activity", this.toString())
        // Log.d("activity", this::activity.toString())


        // (Seongouk Kim) I omitted this part to ensure that this code works on backround too.
        // Will modify it if this cause errors
        // if (!this::activity.isInitialized) {
        //     return events
        // }

        // (Seongouk Kim) This part and above part seems redundant since we'll run this code on background service.
        // if(!usage.hasNextEvent()) {
        //     val intent : Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        //     activity.startActivity(intent)
        // }

        while(usage.hasNextEvent()) {
            var event : UsageEvents.Event = UsageEvents.Event()
            usage.getNextEvent(event)
            
            if(event.getPackageName().contains("instant_app")) continue // Ignore instant apps

            if(packageName == null || packageName == event.getPackageName()) {
            val pm : PackageManager = context.getPackageManager()
            val name : String = if(event.getPackageName() == null) "" else event.getPackageName()!!
            val applicationInfo : ApplicationInfo = pm.getApplicationInfo(name, 0)
            events.add(mapOf(
                "className" to event.getClassName(),
                "category" to applicationInfo.category.toString(),
                "eventType" to event.getEventType().toString(),
                "packageName" to event.getPackageName().toString(),
                "timeStamp" to event.getTimeStamp().toString(),
            )) 
            }
        }

        /// Return the result
        return events
    }
    

    // Call usage access setting screen
    fun getAppUsagePermission()
    {
        // check permission fisrt
        val allowed = isUsageStatsPermissionGranted()
        if (this::activity.isInitialized && !allowed){
            val intent : Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            activity.startActivity(intent)
        }
        
    }

    // Check whether GET_USAGE_STATS is granted
    private fun isUsageStatsPermissionGranted(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            "com.mentalhealth.mthapp"
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    fun checkIfStatsAreAvailable(@NonNull startTime: Long, @NonNull endTime: Long) : Boolean {
        var usageStatsManager : UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var usage : UsageEvents = usageStatsManager.queryEvents(startTime, endTime)
    
        // Return whether or not stats are available
        return usage.hasNextEvent()
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
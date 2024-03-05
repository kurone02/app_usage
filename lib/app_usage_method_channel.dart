import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'app_usage_platform_interface.dart';

/// An implementation of [AppUsagePlatform] that uses method channels.
class MethodChannelAppUsage extends AppUsagePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('app_usage.methodChannel');

  @override
  Future<String?> getPlatformVersion() async {
    final String? version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<dynamic> getAppUsage(int start, int end, String? packageName) async {
    // print("test from getAppUsage of methd channel");
    Map<String, dynamic> interval = {
      'start': start,
      'end': end,
      'packageName': packageName,
    };
    return await methodChannel.invokeMethod<dynamic>("getAppUsage", interval);
  }
}

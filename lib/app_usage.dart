import 'app_usage_platform_interface.dart';

enum AppCategory {
  UNDEFINED,
  GAME,
  AUDIO,
  VIDEO,
  IMAGE,
  SOCIAL,
  NEWS,
  MAPS,
  PRODUCTIVITY,
  ACCESSIBILITY,
}

/// A dart object contains information of a specific app event.
/// className = the class in the app that invoke the event.
/// eventType = the type of event that occured.
/// Please refer to https://developer.android.com/reference/kotlin/android/app/usage/UsageEvents.Event#constants
/// packageName = the app's name
/// timeStamp = the moment that the event occurs
class AppUsageInfo {
  final String? className;
  final int eventType;
  final String? packageName;
  final String category;
  late DateTime timeStamp;

  AppUsageInfo({
    this.className,
    required this.eventType,
    required this.packageName,
    required this.category,
    required this.timeStamp,
  });
}

/// A dart object contains all app events such as opening, closing, etc. in a specific time range
class AppUsage {
  /// Just for testing, don't need to worry about this method...
  Future<String?> getPlatformVersion() {
    return AppUsagePlatform.instance.getPlatformVersion();
  }

  /// Get all app event from [startTime] to [endTime].
  /// It returns a list of AppUsageInfo.
  Future<List<AppUsageInfo>> getUsage(
    DateTime startTime,
    DateTime endTime,
    String? packageName,
  ) async {
    int end = endTime.millisecondsSinceEpoch;
    int start = startTime.millisecondsSinceEpoch;
    final dynamic res = await AppUsagePlatform.instance.getAppUsage(
      start,
      end,
      packageName,
    );
    print("instance: ${AppUsagePlatform.instance}");
    print("res: $res");

    final List<AppUsageInfo> result = [];

    for (var event in res) {
      event = Map.of(event);
      result.add(AppUsageInfo(
        className: event["className"],
        eventType: int.parse(event["eventType"]!),
        packageName: event["packageName"],
        category:
            AppCategory.values[int.parse(event["category"]) + 1].toString(),
        timeStamp:
            DateTime.fromMillisecondsSinceEpoch(int.parse(event["timeStamp"]!)),
      ));
    }

    return result;
  }
}

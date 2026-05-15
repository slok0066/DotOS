import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'core/components.dart';
import 'core/theme.dart';

class WidgetSelectionScreen extends StatefulWidget {
  final String category;
  final String title;
  final VoidCallback? onToggleTheme;
  
  const WidgetSelectionScreen({
    super.key,
    required this.category,
    required this.title,
    this.onToggleTheme,
  });

  @override
  State<WidgetSelectionScreen> createState() => _WidgetSelectionScreenState();
}

class _WidgetSelectionScreenState extends State<WidgetSelectionScreen> {
  static const _clockFormatKey = 'clock_24hour';
  bool _is24Hour = true;
  String _widgetTheme = 'dark';

  @override
  void initState() {
    super.initState();
    _loadClockFormat();
    _loadWidgetTheme();
  }

  Future<void> _loadClockFormat() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getBool(_clockFormatKey);
    if (saved != null && mounted) {
      setState(() {
        _is24Hour = saved;
      });
    }
  }

  Future<void> _toggleClockFormat() async {
    HapticFeedback.selectionClick();
    final newFormat = !_is24Hour;
    setState(() {
      _is24Hour = newFormat;
    });
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_clockFormatKey, newFormat);
  }

  Future<void> _loadWidgetTheme() async {
    final prefs = await SharedPreferences.getInstance();
    final key = 'widget_theme_${widget.category}';
    final saved = prefs.getString(key);
    if (saved != null && (saved == 'dark' || saved == 'light') && mounted) {
      setState(() {
        _widgetTheme = saved;
      });
    } else {
      final fallback = WidgetsBinding.instance.platformDispatcher.platformBrightness == Brightness.dark ? 'dark' : 'light';
      setState(() {
        _widgetTheme = fallback;
      });
    }
  }

  Future<void> _toggleWidgetTheme() async {
    HapticFeedback.selectionClick();
    final newTheme = _widgetTheme == 'dark' ? 'light' : 'dark';
    setState(() {
      _widgetTheme = newTheme;
    });
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('widget_theme_${widget.category}', newTheme);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = Theme.of(context).scaffoldBackgroundColor;
    final textPrimary = Theme.of(context).colorScheme.onSurface;
    final textSecondary = NothingTheme.textSecondary;
    final borderColor = Theme.of(context).colorScheme.outline;
    final surfaceColor = Theme.of(context).colorScheme.surface;
    final surfaceRaised = isDark ? NothingTheme.surfaceRaised : const Color(0xFFF0F0F0);
    final innerSurface = isDark ? NothingTheme.black : surfaceColor;
    
    return Scaffold(
      backgroundColor: bgColor,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 24, 24, 12),
              child: Row(
                children: [
                  GestureDetector(
                    onTap: () {
                      HapticFeedback.lightImpact();
                      Navigator.pop(context);
                    },
                    child: Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        border: Border.all(color: borderColor, width: 1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Icon(
                        Icons.arrow_back,
                        color: textPrimary,
                        size: 18,
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      widget.title.toUpperCase(),
                      style: NothingTheme.doto(fontSize: 26, color: textPrimary),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  const SizedBox(width: 8),
                  // Widget theme toggle - pill style with label
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        'WIDGET THEME',
                        style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                      ),
                      const SizedBox(width: 8),
                      GestureDetector(
                        onTap: _toggleWidgetTheme,
                        child: Tooltip(
                          message: 'Widget theme: ${_widgetTheme.toUpperCase()}',
                          child: AnimatedContainer(
                            duration: const Duration(milliseconds: 250),
                            height: 36,
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            decoration: BoxDecoration(
                              color: _widgetTheme == 'dark' ? textPrimary : borderColor,
                              border: Border.all(color: borderColor, width: 1),
                              borderRadius: BorderRadius.circular(18),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(
                                  _widgetTheme == 'dark' ? Icons.dark_mode : Icons.light_mode,
                                  color: _widgetTheme == 'dark'
                                      ? (isDark ? NothingTheme.black : Colors.white)
                                      : textSecondary,
                                  size: 14,
                                ),
                                const SizedBox(width: 6),
                                Text(
                                  _widgetTheme.toUpperCase(),
                                  style: NothingTheme.mono(
                                    fontSize: 9,
                                    color: _widgetTheme == 'dark'
                                        ? (isDark ? NothingTheme.black : Colors.white)
                                        : textSecondary,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(width: 8),
                  // App theme toggle
                  GestureDetector(
                    onTap: () {
                      HapticFeedback.lightImpact();
                      widget.onToggleTheme?.call();
                    },
                    child: Tooltip(
                      message: 'Toggle app theme',
                      child: Container(
                        width: 40,
                        height: 40,
                        decoration: BoxDecoration(
                          border: Border.all(color: borderColor, width: 1),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Icon(
                          isDark ? Icons.light_mode_outlined : Icons.dark_mode_outlined,
                          color: textSecondary,
                          size: 18,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            Expanded(
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 300),
                child: Column(
                  key: ValueKey('${widget.category}-$_widgetTheme-$_is24Hour'),
                  children: [
                    Expanded(
                      child: GridView.builder(
                        key: ValueKey('grid-${widget.category}'),
                        padding: EdgeInsets.fromLTRB(24, 12, 24, widget.category == 'clock' ? 12 : 24),
                        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          crossAxisSpacing: 16,
                          mainAxisSpacing: 16,
                          childAspectRatio: widget.category == 'music' ? 0.82 : 0.85,
                        ),
                        itemCount: widget.category == 'clock'
                          ? 3
                          : (widget.category == 'calculator'
                            ? 1
                            : (widget.category == 'utilities'
                              ? 3
                              : (widget.category == 'games'
                                ? 5
                                : (widget.category == 'music'
              ? 5
                                  : ((widget.category == 'sound' || widget.category == 'screen_time')
                                      ? 3
                                      : (widget.category == 'storage' ? 4 : (widget.category == 'calendar' ? 4 : 5))))))),
                        itemBuilder: (context, index) {
                          return TweenAnimationBuilder<double>(
                            key: ValueKey('card-$index'),
                            tween: Tween(begin: 0.0, end: 1.0),
                            duration: Duration(milliseconds: 300 + (index * 60)),
                            curve: const Cubic(0.2, 0.0, 0.0, 1.0),
                            builder: (context, value, child) {
                              return Opacity(
                                opacity: value,
                                child: Transform.scale(
                                  scale: 0.95 + (0.05 * value),
                                  child: child,
                                ),
                              );
                            },
                            child: _buildWidgetGridCard(
                              context,
                              index + 1,
                              isDark,
                              textPrimary,
                              textSecondary,
                              borderColor,
                              surfaceRaised,
                              innerSurface,
                            ),
                          );
                        },
                      ),
                    ),
                    if (widget.category == 'clock')
                      Padding(
                        padding: const EdgeInsets.fromLTRB(24, 0, 24, 8),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          decoration: BoxDecoration(
                            color: surfaceRaised,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: borderColor, width: 1),
                          ),
                          child: Row(
                            children: [
                              Icon(Icons.access_time, size: 14, color: textSecondary),
                              const SizedBox(width: 8),
                              Text(
                                'TIME FORMAT',
                                style: NothingTheme.mono(fontSize: 9, color: textSecondary),
                              ),
                              const Spacer(),
                              GestureDetector(
                                onTap: _toggleClockFormat,
                                child: AnimatedContainer(
                                  duration: const Duration(milliseconds: 200),
                                  height: 32,
                                  decoration: BoxDecoration(
                                    border: Border.all(color: borderColor, width: 1),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: Row(
                                    children: [
                                      AnimatedContainer(
                                        duration: const Duration(milliseconds: 200),
                                        width: 48,
                                        height: 32,
                                        decoration: BoxDecoration(
                                          color: !_is24Hour ? textPrimary : Colors.transparent,
                                          borderRadius: BorderRadius.circular(7),
                                        ),
                                        child: Center(
                                          child: Text(
                                            '12H',
                                            style: NothingTheme.mono(
                                              fontSize: 10,
                                              color: !_is24Hour
                                                  ? (isDark ? NothingTheme.black : Colors.white)
                                                  : textSecondary,
                                            ),
                                          ),
                                        ),
                                      ),
                                      AnimatedContainer(
                                        duration: const Duration(milliseconds: 200),
                                        width: 48,
                                        height: 32,
                                        decoration: BoxDecoration(
                                          color: _is24Hour ? textPrimary : Colors.transparent,
                                          borderRadius: BorderRadius.circular(7),
                                        ),
                                        child: Center(
                                          child: Text(
                                            '24H',
                                            style: NothingTheme.mono(
                                              fontSize: 10,
                                              color: _is24Hour
                                                  ? (isDark ? NothingTheme.black : Colors.white)
                                                  : textSecondary,
                                            ),
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildWidgetGridCard(
    BuildContext context,
    int styleNumber,
    bool isDark,
    Color textPrimary,
    Color textSecondary,
    Color borderColor,
    Color surfaceRaised,
    Color innerSurface,
  ) {
    final widgetType = _getWidgetType(styleNumber);
    final styleName = _getStyleName(styleNumber);
    final sizeLabel = _getWidgetSizeLabel(widgetType);

    return GestureDetector(
      onTap: () {
        HapticFeedback.mediumImpact();
        _addWidget(context, widgetType);
      },
      child: widgetType == 'calculator_01'
          ? _buildMiniatureCalculatorCard(context, styleName, sizeLabel, isDark, textPrimary, textSecondary, borderColor)
          : Container(
        decoration: BoxDecoration(
          color: surfaceRaised,
          border: Border.all(color: borderColor, width: 1),
          borderRadius: BorderRadius.circular(16),
        ),
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: innerSurface,
                  border: Border.all(color: borderColor, width: 1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(10),
                  child: Center(
                    child: _buildWidgetPreview(
                      widgetType,
                      isDark,
                      textPrimary,
                      textSecondary,
                      borderColor,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 10),
            Text(
              styleName,
              style: NothingTheme.grotesk(fontSize: 14, color: textPrimary),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 6),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  sizeLabel,
                  style: NothingTheme.mono(fontSize: 9, color: textSecondary),
                ),
                Container(
                  width: 28,
                  height: 28,
                  decoration: BoxDecoration(
                    color: textPrimary,
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Icon(
                    Icons.add,
                    size: 14,
                    color: isDark ? NothingTheme.black : const Color(0xFFFFFFFF),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _getWidgetType(int styleNumber) {
    switch (widget.category) {
      case 'clock':
        return styleNumber == 1 ? 'clock' : 'clock_0$styleNumber';
      case 'calendar':
        return 'calendar_0$styleNumber';
      case 'battery':
        return 'battery_0$styleNumber';
case 'storage':
         return 'storage_0$styleNumber';
      case 'calculator':
        return 'calculator_01';
      case 'utilities':
        return 'tap_counter_0$styleNumber';
      case 'sound':
        return 'sound_0$styleNumber';
      case 'screen_time':
        return 'screen_time_0$styleNumber';
      case 'games':
        return styleNumber == 1 ? 'spinner' : (styleNumber == 2 ? 'bottle_spin' : (styleNumber == 3 ? 'dice_roll' : (styleNumber == 4 ? 'dino_game' : 'coin_flip')));
      case 'music':
        return styleNumber == 1
          ? 'music_01'
          : (styleNumber == 2
            ? 'music_02'
            : (styleNumber == 3
              ? 'music_03'
              : (styleNumber == 4 ? 'music_04' : 'music_05')));

      default:
        return 'clock';
    }
  }

  String _getStyleName(int styleNumber) {
    switch (widget.category) {
case 'clock':
         return ['Dot-Matrix', 'Analog', 'Binary'][styleNumber - 1];
      case 'calendar':
        return ['Day Focus', 'Week View', 'Events', 'Month', 'Agenda'][styleNumber - 1];
      case 'battery':
        return ['Percentage', 'Circular', 'Bars', 'Minimal', 'Detailed'][styleNumber - 1];
case 'storage':
         return ['Usage', 'Ring', 'Compact', 'Analysis', 'Detailed'][styleNumber - 1];
      case 'calculator':
        return 'Standard';
      case 'utilities':
        return ['Classic', 'Dial', 'Matrix'][styleNumber - 1];
      case 'sound':
        return ['Classic', 'Segments', 'Dial'][styleNumber - 1];
      case 'screen_time':
        return ['Minimal', 'Ring', 'Split'][styleNumber - 1];
      case 'games':
        return ['Spinner', 'Bottle Spin', 'Dice Roll', 'Dino Game', 'Coin Flip'][styleNumber - 1];
      case 'music':
        return ['Ring', 'Vinyl', 'Compact', 'Ticker', 'Wave'][styleNumber - 1];
      default:
        return 'Widget $styleNumber';
    }
  }

String _getWidgetSizeLabel(String widgetType) {
     switch (widgetType) {
       case 'clock':
       case 'clock_02':
       case 'clock_03':
       case 'calendar_01':
       case 'calendar_02':
       case 'calendar_03':
       case 'battery_01':
       case 'battery_02':
       case 'battery_03':
       case 'storage_01':
       case 'storage_02':
       case 'music_01':
         return '4×2';
       case 'calendar_04':
       case 'battery_04':
       case 'storage_03':
       case 'storage_04':
       case 'screen_time_01':
       case 'screen_time_02':
       case 'screen_time_03':
       case 'tap_counter_01':
       case 'tap_counter_02':
       case 'tap_counter_03':
       case 'sound_01':
       case 'sound_02':
       case 'sound_03':
       case 'spinner':
       case 'bottle_spin':
       case 'dice_roll':
       case 'dino_game':
       case 'coin_flip':
       case 'music_02':
       case 'music_03':
       case 'music_05':
         return '2×2';
        case 'battery_05':
        case 'storage_05':
          return '4×2';
       case 'calculator_01':
         return '4×5';
       case 'music_04':
         return '4×1';
       default:
         return '2×2';
     }
   }

  Widget _buildWidgetPreview(String widgetType, bool isDark, Color textPrimary, Color textSecondary, Color borderColor) {
    switch (widgetType) {
      // Clock widgets
      case 'clock':
        return _buildClockPreview(textPrimary, textSecondary);
      case 'clock_02':
        return _buildAnalogClockPreview(textPrimary);
      case 'clock_03':
        return _buildBinaryClockPreview(textPrimary, textSecondary, borderColor);
      
      // Calendar widgets
      case 'calendar_01':
        return _buildCalendarPreview(textPrimary, textSecondary, borderColor);
      case 'calendar_02':
        return _buildWeekCalendarPreview(textSecondary, borderColor);
      case 'calendar_03':
        return _buildEventListPreview(textPrimary, textSecondary);
      case 'calendar_04':
        return _buildMonthGridPreview(textSecondary, borderColor);
      
      // Battery widgets
      case 'battery_01':
        return _buildBatteryPreview(textPrimary, textSecondary);
      case 'battery_02':
        return _buildCircularBatteryPreview(textPrimary);
      case 'battery_03':
        return _buildBarsBatteryPreview(textPrimary, borderColor);
      case 'battery_04':
        return _buildMinimalBatteryPreview(textPrimary, textSecondary);
      case 'battery_05':
        return _buildDetailedBatteryPreview(textPrimary, textSecondary);
      
      // Storage widgets
      case 'storage_01':
        return _buildStoragePreview(textPrimary, textSecondary, isDark);
      case 'storage_02':
        return _buildCircularStoragePreview(textPrimary);
case 'storage_03':
         return _buildCompactStoragePreview(textPrimary, textSecondary, borderColor);
       case 'storage_04':
        return _buildStorageBreakdownPreview(textPrimary, textSecondary, borderColor);
      case 'storage_05':
        return _buildDetailedStoragePreview(textPrimary, textSecondary, borderColor);
      
case 'calculator_01':
         return _buildCalculatorPreview(textPrimary, textSecondary, borderColor);
      
      // Utilities widgets
      case 'tap_counter_01':
        return _buildTapCounterPreview(textPrimary, textSecondary);
      case 'tap_counter_02':
        return _buildTapDialPreview(textPrimary, textSecondary);
      case 'tap_counter_03':
        return _buildTapMatrixPreview(textPrimary, textSecondary);
      
      // Sound widgets
      case 'sound_01':
        return _buildSoundPreview(textPrimary, textSecondary);
      case 'sound_02':
        return _buildSoundSegmentsPreview(isDark, textPrimary, textSecondary, borderColor);
      case 'sound_03':
        return _buildSoundDialPreview(textPrimary, textSecondary, borderColor);
      
      // Screen Time widgets
      case 'screen_time_01':
        return _buildScreenTimeMinimalPreview(textPrimary, textSecondary, borderColor);
      case 'screen_time_02':
        return _buildScreenTimeRingPreview(textPrimary, textSecondary, borderColor);
      case 'screen_time_03':
        return _buildScreenTimeSplitPreview(textPrimary, textSecondary, borderColor);

      // Game widgets
      case 'spinner':
        return _buildSpinnerPreview(textPrimary, textSecondary);
      case 'bottle_spin':
        return _buildBottleSpinPreview(textPrimary, textSecondary);
      case 'dice_roll':
        return _buildDiceRollPreview(textPrimary, textSecondary);
      case 'coin_flip':
        return _buildCoinFlipPreview(textPrimary, textSecondary);
      case 'dino_game':
        return _buildDinoGamePreview(textPrimary, textSecondary, borderColor);

      // Music widgets
      case 'music_01':
        return _buildMusicRingPreview(textPrimary, textSecondary, borderColor);
      case 'music_02':
        return _buildMusicVinylPreview(textPrimary, textSecondary);
      case 'music_03':
        return _buildMusicCompactPreview(textPrimary, textSecondary, borderColor);
      case 'music_04':
        return _buildMusicTickerPreview(textPrimary, textSecondary, borderColor);
      case 'music_05':
        return _buildMusicWavePreview(textPrimary, textSecondary, borderColor);

      default:
        return Center(
          child: Text(
            '[PREVIEW]',
            style: NothingTheme.mono(fontSize: 10, color: textSecondary),
          ),
        );
    }
  }
  Widget _buildMiniatureCalculatorCard(
    BuildContext context,
    String styleName,
    String sizeLabel,
    bool isDark,
    Color textPrimary,
    Color textSecondary,
    Color borderColor,
  ) {
    final calcBg = isDark ? const Color(0xFF000000) : const Color(0xFFF5F5F0);
    final calcBorder = isDark ? const Color(0xFF222222) : const Color(0xFFDDDDDD);
    final calcText = isDark ? const Color(0xFFFFFFFF) : const Color(0xFF000000);
    final calcLabel = isDark ? const Color(0xFF999999) : const Color(0xFF666666);

    return Container(
      decoration: BoxDecoration(
        color: calcBg,
        border: Border.all(color: calcBorder, width: 1),
        borderRadius: BorderRadius.circular(24),
      ),
      padding: const EdgeInsets.all(12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                Text(
                  '12+7',
                  style: NothingTheme.mono(fontSize: 10, color: calcLabel),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                Text(
                  '19',
                  style: NothingTheme.doto(fontSize: 24, color: calcText),
                  maxLines: 1,
                ),
                const SizedBox(height: 8),
                Expanded(
                  child: Column(
                    children: [
                      _calcPreviewRow(['C', 'B', '/'], isTop: true, isDark: isDark),
                      _calcPreviewRow(['7', '8', '9', 'x'], isDark: isDark),
                      _calcPreviewRow(['4', '5', '6', '-'], isDark: isDark),
                      _calcPreviewRow(['1', '2', '3', '+'], isDark: isDark),
                      _calcPreviewRow(['0', '.', '='], isBottom: true, isDark: isDark),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 10),
          Text(
            styleName,
            style: NothingTheme.grotesk(fontSize: 14, color: calcText),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 6),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                sizeLabel,
                style: NothingTheme.mono(fontSize: 9, color: calcLabel),
              ),
              Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  color: calcText,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Icon(
                  Icons.add,
                  size: 14,
                  color: calcBg,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _calcPreviewRow(List<String> labels, {bool isTop = false, bool isBottom = false, bool isDark = true}) {
    final btnBg = isDark ? const Color(0xFF1A1A1A) : const Color(0xFFE8E8E8);
    final btnBorder = isDark ? const Color(0xFF333333) : const Color(0xFFCCCCCC);
    final btnFg = isDark ? const Color(0xFFFFFFFF) : const Color(0xFF000000);
    final btnAccent = const Color(0xFFD71921);
    final btnAccentFg = const Color(0xFF000000);

    return Expanded(
      child: Padding(
        padding: const EdgeInsets.only(bottom: 2.0),
        child: Row(
          children: labels.map((label) {
            Color bgColor = btnBg;
            Color fgColor = btnFg;
            
            if (label == '=' && isBottom) {
              bgColor = btnAccent;
              fgColor = btnAccentFg;
            } else if (label == 'C' || label == '/' || label == 'x' || label == '-' || label == '+') {
              fgColor = btnAccent;
            }

            int flex = 1;
            if (isTop && label == 'C') flex = 2;
            if (isBottom && label == '0') flex = 2;

            return Expanded(
              flex: flex,
              child: Container(
                margin: const EdgeInsets.only(right: 2.0),
                decoration: BoxDecoration(
                  color: bgColor,
                  border: Border.all(color: btnBorder, width: 0.5),
                  borderRadius: BorderRadius.circular(100),
                ),
                child: Center(
                  child: Text(
                    label,
                    style: NothingTheme.mono(fontSize: 8, color: fgColor),
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ),
    );
  }

  Widget _buildClockPreview(Color textPrimary, Color textSecondary) {
    return _LiveClockPreview(
      textPrimary: textPrimary,
      textSecondary: textSecondary,
      is24Hour: _is24Hour,
    );
  }

  Widget _buildAnalogClockPreview(Color textPrimary) {
    final now = DateTime(2026, 5, 15, 17, 30, 0);
    final displayTime = _is24Hour
        ? DateFormat('HH:mm').format(now)
        : DateFormat('h:mm').format(now);
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('ANALOG', style: NothingTheme.mono(fontSize: 7, color: NothingTheme.textSecondary)),
              const SizedBox(height: 6),
              FittedBox(
                fit: BoxFit.scaleDown,
                alignment: Alignment.centerLeft,
                child: Text(
                  displayTime,
                  style: NothingTheme.grotesk(fontSize: 16, color: textPrimary, fontWeight: FontWeight.bold),
                ),
              ),
              if (!_is24Hour)
                Text(
                  DateFormat('a').format(now),
                  style: NothingTheme.mono(fontSize: 8, color: NothingTheme.accent),
                ),
            ],
          ),
        ),
        const SizedBox(width: 8),
        Container(
          width: 44,
          height: 44,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: textPrimary.withValues(alpha: 0.5), width: 1.5),
          ),
          child: CustomPaint(
            painter: _AnalogClockPainter(textPrimary),
          ),
        ),
      ],
    );
  }

  Widget _buildBinaryClockPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    final now = DateTime(2026, 5, 15, 17, 30, 0);
    var hour = now.hour;
    final minute = now.minute;
    String periodLabel = '';
    if (!_is24Hour) {
      periodLabel = hour >= 12 ? 'PM' : 'AM';
      hour = hour % 12;
      if (hour == 0) hour = 12;
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text('BINARY', style: NothingTheme.mono(fontSize: 7, color: textSecondary)),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                _buildBinaryColumn(hour ~/ 10, 'H', textPrimary, borderColor),
                const SizedBox(width: 4),
                _buildBinaryColumn(hour % 10, 'H', textPrimary, borderColor),
                const SizedBox(width: 8),
                _buildBinaryColumn(minute ~/ 10, 'M', textPrimary, borderColor),
                const SizedBox(width: 4),
                _buildBinaryColumn(minute % 10, 'M', textPrimary, borderColor),
              ],
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                FittedBox(
                  fit: BoxFit.scaleDown,
                  alignment: Alignment.centerRight,
                  child: Text(
                    '${hour.toString().padLeft(2, '0')}:${minute.toString().padLeft(2, '0')}$periodLabel',
                    style: NothingTheme.grotesk(fontSize: 14, color: textPrimary, fontWeight: FontWeight.bold),
                  ),
                ),
                Text('DECIMAL', style: NothingTheme.mono(fontSize: 5, color: textSecondary)),
              ],
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildBinaryColumn(int value, String label, Color textPrimary, Color borderColor) {
    final textSecondary = NothingTheme.textSecondary;
    return Column(
      mainAxisAlignment: MainAxisAlignment.end,
      children: [
        ...List.generate(4, (i) {
          final isOn = (value & (1 << (3 - i))) != 0;
          return Container(
            width: 6,
            height: 6,
            margin: const EdgeInsets.only(bottom: 3),
            decoration: BoxDecoration(
              color: isOn ? textPrimary : borderColor,
              shape: BoxShape.circle,
            ),
          );
        }),
        const SizedBox(height: 2),
        Text('$value', style: NothingTheme.mono(fontSize: 7, color: textSecondary)),
        Text(label, style: NothingTheme.mono(fontSize: 5, color: textSecondary.withValues(alpha: 0.6))),
      ],
    );
  }

  // Calendar Previews
  Widget _buildCalendarPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Container(
          padding: const EdgeInsets.all(6),
          decoration: BoxDecoration(
            border: Border.all(color: borderColor),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Text(
            '30',
            style: NothingTheme.doto(fontSize: 18, color: textPrimary),
          ),
        ),
        const SizedBox(width: 6),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                'CALENDAR',
                style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 2),
              Text(
                'APRIL 26',
                style: NothingTheme.mono(fontSize: 9, color: textPrimary),
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildWeekCalendarPreview(Color textSecondary, Color borderColor) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          'WEEK',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 8),
        FittedBox(
          fit: BoxFit.scaleDown,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: ['M', 'T', 'W', 'T', 'F', 'S', 'S'].map((day) {
              return Container(
                width: 12,
                height: 12,
                margin: const EdgeInsets.symmetric(horizontal: 1),
                decoration: BoxDecoration(
                  border: Border.all(
                    color: day == 'W' ? const Color(0xFFD71921) : borderColor,
                    width: 1,
                  ),
                  borderRadius: BorderRadius.circular(2),
                ),
                child: Center(
                  child: Text(
                    day,
                    style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                  ),
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  Widget _buildEventListPreview(Color textPrimary, Color textSecondary) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          'EVENTS',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 8),
        _buildEventItem('10:00', 'MEETING', textPrimary, textSecondary),
        _buildEventItem('14:30', 'CALL', textPrimary, textSecondary),
        _buildEventItem('16:00', 'REVIEW', textPrimary, textSecondary),
      ],
    );
  }

  Widget _buildEventItem(String time, String title, Color textPrimary, Color textSecondary) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6.0),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 4,
            height: 4,
            decoration: const BoxDecoration(
              color: Color(0xFFD71921),
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 4),
          Text(
            time,
            style: NothingTheme.mono(fontSize: 8, color: textSecondary),
          ),
          const SizedBox(width: 4),
          Flexible(
            child: Text(
              title,
              style: NothingTheme.mono(fontSize: 8, color: textPrimary),
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMonthGridPreview(Color textSecondary, Color borderColor) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          'APRIL',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 6),
        ...List.generate(4, (week) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 2.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              mainAxisSize: MainAxisSize.min,
              children: List.generate(7, (day) {
                final dayNum = week * 7 + day + 1;
                final isToday = dayNum == 15;
                return Container(
                  width: 10,
                  height: 10,
                  margin: const EdgeInsets.symmetric(horizontal: 1),
                  decoration: BoxDecoration(
                    border: isToday ? Border.all(color: const Color(0xFFD71921), width: 1) : null,
                    borderRadius: BorderRadius.circular(2),
                  ),
                  child: Center(
                    child: Text(
                      dayNum > 30 ? '' : '$dayNum',
                      style: NothingTheme.mono(fontSize: 5, color: textSecondary),
                    ),
                  ),
                );
              }),
            ),
          );
        }),
      ],
    );
  }

  // Battery Previews
  Widget _buildBatteryPreview(Color textPrimary, Color textSecondary) {
    return FittedBox(
      fit: BoxFit.scaleDown,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text('BATTERY', style: NothingTheme.mono(fontSize: 10, color: textSecondary)),
          const SizedBox(height: 8),
          Text('85%', style: NothingTheme.doto(fontSize: 48, color: textPrimary)),
          const SizedBox(height: 8),
          Text('CHARGING', style: NothingTheme.mono(fontSize: 10, color: const Color(0xFF00C853))),
        ],
      ),
    );
  }

  Widget _buildCircularBatteryPreview(Color textPrimary) {
    return Center(
      child: Container(
        width: 100,
        height: 100,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: Border.all(color: const Color(0xFF00C853), width: 6),
        ),
        child: Center(
          child: Text(
            '85',
            style: NothingTheme.mono(fontSize: 20, color: textPrimary),
          ),
        ),
      ),
    );
  }

  Widget _buildBarsBatteryPreview(Color textPrimary, Color borderColor) {
    return FittedBox(
      fit: BoxFit.scaleDown,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Text('85%', style: NothingTheme.doto(fontSize: 32, color: textPrimary)),
          const SizedBox(width: 16),
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(10, (i) {
              return Container(
                width: 50,
                height: 7,
                margin: const EdgeInsets.only(bottom: 3),
                decoration: BoxDecoration(
                  color: i < 8 ? const Color(0xFF00C853) : borderColor,
                  borderRadius: BorderRadius.circular(2),
                ),
              );
            }),
          ),
        ],
      ),
    );
  }

  Widget _buildMinimalBatteryPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          '85',
          style: NothingTheme.doto(fontSize: 56, color: textPrimary),
        ),
        Text(
          '%',
          style: NothingTheme.mono(fontSize: 16, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildDetailedBatteryPreview(Color textPrimary, Color textSecondary) {
    return FittedBox(
      fit: BoxFit.scaleDown,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text('85%', style: NothingTheme.doto(fontSize: 32, color: textPrimary)),
          const SizedBox(width: 20),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _buildStatLine('VOLT', '4.2V', textPrimary, textSecondary),
              _buildStatLine('TEMP', '32°C', textPrimary, textSecondary),
              _buildStatLine('TIME', '8H', textPrimary, textSecondary),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatLine(String label, String value, Color textPrimary, Color textSecondary) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6.0),
      child: Row(
        children: [
          SizedBox(
            width: 45,
            child: Text(
              label,
              style: NothingTheme.mono(fontSize: 8, color: textSecondary),
            ),
          ),
          Text(
            value,
            style: NothingTheme.mono(fontSize: 10, color: textPrimary),
          ),
        ],
      ),
    );
  }

  // Storage Previews
  Widget _buildStoragePreview(Color textPrimary, Color textSecondary, bool isDark) {
    return SizedBox(
      width: double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            'STORAGE',
            style: NothingTheme.mono(fontSize: 10, color: textSecondary),
          ),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Flexible(
                child: FittedBox(
                  fit: BoxFit.scaleDown,
                  alignment: Alignment.centerLeft,
                  child: Text(
                    '76.8 GB',
                    style: NothingTheme.doto(fontSize: 28, color: textPrimary),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text(
                  'OF 128',
                  style: NothingTheme.mono(fontSize: 9, color: textSecondary),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: SizedBox(
              width: double.infinity,
              child: LinearProgressIndicator(
                value: 0.6,
                backgroundColor: isDark ? const Color(0xFF222222) : const Color(0xFFE0E0E0),
                valueColor: AlwaysStoppedAnimation(textPrimary),
                minHeight: 6,
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '60% USED',
            style: NothingTheme.mono(fontSize: 9, color: textSecondary),
          ),
        ],
      ),
    );
  }

  Widget _buildCircularStoragePreview(Color textPrimary) {
    return SizedBox(
      width: 176,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('STORAGE', style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textSecondary)),
              const SizedBox(height: 8),
              Text('60%', style: NothingTheme.doto(fontSize: 30, color: textPrimary)),
              const SizedBox(height: 4),
              Text('USED', style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textSecondary)),
            ],
          ),
          SizedBox(
            width: 72,
            height: 72,
            child: Stack(
              alignment: Alignment.center,
              children: [
                SizedBox(
                  width: 72,
                  height: 72,
                  child: CircularProgressIndicator(
                    value: 0.6,
                    strokeWidth: 6,
                    backgroundColor: NothingTheme.border,
                    valueColor: AlwaysStoppedAnimation(textPrimary),
                  ),
                ),
                Text(
                  '76G',
                  style: NothingTheme.mono(fontSize: 11, color: textPrimary),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }



  Widget _buildStorageBar(String label, double value, Color color, Color textSecondary, Color borderColor) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Row(
        children: [
          SizedBox(
            width: 60,
            child: Text(
              label,
              style: NothingTheme.mono(fontSize: 9, color: textSecondary),
            ),
          ),
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(2),
              child: LinearProgressIndicator(
                value: value,
                backgroundColor: borderColor,
                valueColor: AlwaysStoppedAnimation(color),
                minHeight: 6,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMiniStorageBar(String label, double value, Color color, Color textSecondary, Color borderColor) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        children: [
          SizedBox(
            width: 38,
            child: Text(
              label,
              style: NothingTheme.mono(fontSize: 7, color: textSecondary),
            ),
          ),
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(999),
              child: LinearProgressIndicator(
                value: value,
                backgroundColor: borderColor,
                valueColor: AlwaysStoppedAnimation(color),
                minHeight: 5,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCompactStoragePreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return SizedBox(
      width: 132,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            '60%',
            style: NothingTheme.doto(fontSize: 42, color: textPrimary),
          ),
          const SizedBox(height: 4),
          Text(
            '76 / 128 GB',
            style: NothingTheme.mono(fontSize: 9, color: textSecondary),
          ),
          const SizedBox(height: 10),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              value: 0.6,
              backgroundColor: borderColor,
              valueColor: AlwaysStoppedAnimation(textPrimary),
              minHeight: 6,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailedStoragePreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return SizedBox(
      width: 190,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'STORAGE ANALYSIS',
                style: NothingTheme.mono(fontSize: 9, color: textSecondary),
              ),
              Text(
                '128 GB',
                style: NothingTheme.mono(fontSize: 9, color: textSecondary),
              ),
            ],
          ),
          const SizedBox(height: 12),
          _buildStorageBar('APPS', 0.35, textPrimary, textSecondary, borderColor),
          _buildStorageBar('PHOTO', 0.25, textSecondary, textSecondary, borderColor),
          _buildStorageBar('VIDEO', 0.20, borderColor, textSecondary, borderColor),
          _buildStorageBar('OTHER', 0.10, textSecondary.withValues(alpha: 0.55), textSecondary, borderColor),
        ],
      ),
    );
  }

  Widget _buildStorageBreakdownPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return SizedBox(
      width: 138,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text('ANALYSIS', style: NothingTheme.mono(fontSize: 9, color: textSecondary)),
          const SizedBox(height: 10),
          _buildMiniStorageBar('APPS', 0.35, textPrimary, textSecondary, borderColor),
          _buildMiniStorageBar('MEDIA', 0.25, textSecondary, textSecondary, borderColor),
          _buildMiniStorageBar('FREE', 0.40, textSecondary.withValues(alpha: 0.55), textSecondary, borderColor),
        ],
      ),
    );
  }

  Future<void> _addWidget(BuildContext context, String widgetType) async {
    try {
      if (_isScreenTimeWidget(widgetType)) {
        final granted = await _ensureUsageStatsPermission(context);
        if (!granted) {
          return;
        }
      }

      final success = await WidgetService.addWidget(
        widgetType,
        theme: _widgetTheme,
      );
      
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              success ? 'WIDGET ADDED' : 'FAILED TO ADD WIDGET',
              style: NothingTheme.mono(fontSize: 12, color: const Color(0xFF000000)),
            ),
            backgroundColor: success ? const Color(0xFF00C853) : const Color(0xFFD71921),
            duration: const Duration(seconds: 2),
          ),
        );
        if (success) {
          Navigator.pop(context);
        }
      }
    } on PlatformException catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'ERROR: ${e.message}',
              style: NothingTheme.mono(fontSize: 12, color: const Color(0xFF000000)),
            ),
            backgroundColor: const Color(0xFFD71921),
          ),
        );
      }
    }
  }

  bool _isScreenTimeWidget(String widgetType) => widgetType.startsWith('screen_time_');

  static const _permissionChannel = MethodChannel('permission_channel');

  Future<bool> _ensureUsageStatsPermission(BuildContext context) async {
    final granted = await _permissionChannel.invokeMethod<bool>('checkPermission', {'type': 'usage_stats'}) ?? false;
    if (granted) {
      return true;
    }

    await _permissionChannel.invokeMethod<bool>('requestPermission', {'type': 'usage_stats'});

    final refreshed = await _permissionChannel.invokeMethod<bool>('checkPermission', {'type': 'usage_stats'}) ?? false;
    if (!refreshed && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'ENABLE USAGE ACCESS FOR REAL SCREEN TIME DATA',
            style: NothingTheme.mono(fontSize: 12, color: const Color(0xFF000000)),
          ),
          backgroundColor: const Color(0xFFD71921),
          duration: const Duration(seconds: 3),
        ),
      );
    }
    return refreshed;
  }
  Widget _buildTapCounterPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          '12',
          style: NothingTheme.doto(fontSize: 48, color: textPrimary),
        ),
        const SizedBox(height: 16),
        Text(
          'TAPS',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildTapDialPreview(Color textPrimary, Color textSecondary) {
    return Center(
      child: Container(
        width: 100,
        height: 100,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: Border.all(color: textPrimary, width: 2),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              '12',
              style: NothingTheme.doto(fontSize: 32, color: textPrimary),
            ),
            const SizedBox(height: 4),
            Text(
              'TAPS',
              style: NothingTheme.mono(fontSize: 8, color: textSecondary),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTapMatrixPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          '0012',
          style: NothingTheme.doto(fontSize: 40, color: textPrimary, letterSpacing: 4),
        ),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(4, (i) => Container(
            width: 4,
            height: 4,
            margin: const EdgeInsets.symmetric(horizontal: 2),
            decoration: BoxDecoration(
              color: i == 3 ? const Color(0xFFD71921) : textSecondary,
              shape: BoxShape.circle,
            ),
          )),
        ),
      ],
    );
  }

  Widget _buildSoundPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          'SOUND',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 12),
        Text(
          'RING',
          style: NothingTheme.doto(fontSize: 36, color: textPrimary),
        ),
        const SizedBox(height: 6),
        Text(
          'TAP TO TOGGLE',
          style: NothingTheme.mono(fontSize: 8, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildSoundSegmentsPreview(
    bool isDark,
    Color textPrimary,
    Color textSecondary,
    Color borderColor,
  ) {
    return SizedBox(
      width: double.infinity,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            'MODE',
            style: NothingTheme.mono(fontSize: 10, color: textSecondary),
          ),
          const SizedBox(height: 12),
          Row(
            children: ['RING', 'VIB', 'SIL'].map((label) {
              final isActive = label == 'VIB';
              return Expanded(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 2),
                  child: Container(
                    height: 28,
                    decoration: BoxDecoration(
                      color: isActive ? textPrimary : Colors.transparent,
                      border: Border.all(color: borderColor, width: 1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    alignment: Alignment.center,
                    child: FittedBox(
                      fit: BoxFit.scaleDown,
                      child: Text(
                        label,
                        style: NothingTheme.mono(
                          fontSize: 8,
                          color: isActive ? (isDark ? NothingTheme.black : Colors.white) : textSecondary,
                        ),
                      ),
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 10),
          Text(
            'TAP TO TOGGLE',
            style: NothingTheme.mono(fontSize: 8, color: textSecondary),
          ),
        ],
      ),
    );
  }

  Widget _buildSoundDialPreview(
    Color textPrimary,
    Color textSecondary,
    Color borderColor,
  ) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          'SOUND',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 10),
        Container(
          width: 86,
          height: 86,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: borderColor, width: 2),
          ),
          child: Center(
            child: Text(
              'RING',
              style: NothingTheme.mono(fontSize: 10, color: textPrimary),
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'TAP TO TOGGLE',
          style: NothingTheme.mono(fontSize: 8, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildScreenTimeMinimalPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          '3H 42M',
          style: NothingTheme.doto(fontSize: 26, color: textPrimary),
        ),
        const SizedBox(height: 8),
        Text('SCREEN TIME', style: NothingTheme.mono(fontSize: 9, color: textSecondary)),
        const SizedBox(height: 10),
        ClipRRect(
          borderRadius: BorderRadius.circular(999),
          child: SizedBox(
            width: 96,
            height: 4,
            child: LinearProgressIndicator(
              value: 0.46,
              backgroundColor: borderColor,
              valueColor: AlwaysStoppedAnimation(textPrimary),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildScreenTimeRingPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Stack(
          alignment: Alignment.center,
          children: [
            SizedBox(
              width: 86,
              height: 86,
              child: CircularProgressIndicator(
                value: 0.46,
                strokeWidth: 6,
                backgroundColor: borderColor,
                valueColor: AlwaysStoppedAnimation(textPrimary),
              ),
            ),
            Text('46%', style: NothingTheme.mono(fontSize: 16, color: textPrimary)),
          ],
        ),
        const SizedBox(height: 8),
        Text('TODAY', style: NothingTheme.mono(fontSize: 9, color: textSecondary)),
      ],
    );
  }

  Widget _buildScreenTimeSplitPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('3H 42M', style: NothingTheme.doto(fontSize: 22, color: textPrimary)),
              const SizedBox(height: 6),
              Text('SCREEN TIME', style: NothingTheme.mono(fontSize: 8, color: textSecondary)),
            ],
          ),
        ),
        Stack(
          alignment: Alignment.center,
          children: [
            SizedBox(
              width: 56,
              height: 56,
              child: CircularProgressIndicator(
                value: 0.46,
                strokeWidth: 5,
                backgroundColor: borderColor,
                valueColor: AlwaysStoppedAnimation(textPrimary),
              ),
            ),
            Text('46%', style: NothingTheme.mono(fontSize: 11, color: textPrimary)),
          ],
        ),
      ],
    );
  }

  /// Reads today's screen time from the platform channel.
  /// Uses the same Android reader as the actual widget to keep previews in sync.
  Widget _buildSpinnerPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        SizedBox(
          width: 64,
          height: 64,
          child: Stack(
            alignment: Alignment.center,
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(color: textPrimary, width: 2),
                ),
              ),
              SizedBox(
                width: 48,
                height: 48,
                child: Stack(
                  alignment: Alignment.center,
                  children: List.generate(4, (index) {
                    return Transform.rotate(
                      angle: index * math.pi / 2,
                      child: Align(
                        alignment: Alignment.topCenter,
                        child: Container(
                          width: 2,
                          height: 8,
                          color: textPrimary,
                        ),
                      ),
                    );
                  }),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'SPINNER',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildBottleSpinPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        SizedBox(
          width: 56,
          height: 64,
          child: Container(
            decoration: BoxDecoration(
              border: Border.all(color: textPrimary, width: 1.5),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Center(
              child: Transform.rotate(
                angle: -0.785,
                child: Container(
                  width: 14,
                  height: 36,
                  decoration: BoxDecoration(
                    border: Border.all(color: textPrimary, width: 1.5),
                    borderRadius: BorderRadius.circular(3),
                  ),
                  child: Align(
                    alignment: Alignment.topCenter,
                    child: Container(
                      width: 14,
                      height: 10,
                      color: textPrimary,
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'BOTTLE',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildDiceRollPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Container(
          width: 72,
          height: 72,
          decoration: BoxDecoration(
            border: Border.all(color: textPrimary, width: 1.5),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Stack(
            children: [
              for (final offset in const [
                Offset(18, 18),
                Offset(52, 18),
                Offset(35, 35),
                Offset(18, 52),
                Offset(52, 52),
              ])
                Positioned(
                  left: offset.dx - 4,
                  top: offset.dy - 4,
                  child: Container(
                    width: 8,
                    height: 8,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: textPrimary,
                    ),
                  ),
                ),
            ],
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'DICE',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 4),
        Text(
          'TAP TO ROLL',
          style: NothingTheme.mono(fontSize: 8, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildCoinFlipPreview(Color textPrimary, Color textSecondary) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Container(
          width: 56,
          height: 56,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: textPrimary, width: 2),
          ),
          child: Center(
            child: Text(
              'H',
              style: NothingTheme.doto(fontSize: 24, color: textPrimary),
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'COIN FLIP',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
      ],
    );
  }

  Widget _buildDinoGamePreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              width: 16,
              height: 18,
               child: CustomPaint(
                 painter: DinoPixelPainter(textPrimary),
               ),
            ),
            const SizedBox(width: 12),
            Column(
              children: [
                Container(
                  width: 6,
                  height: 12,
                  decoration: BoxDecoration(
                    color: textSecondary,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ],
            ),
          ],
        ),
        const SizedBox(height: 8),
        Container(width: 108, height: 1, color: borderColor),
        const SizedBox(height: 8),
        Text(
          'DINO GAME',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
        const SizedBox(height: 4),
        Text(
          'TAP TO START',
          style: NothingTheme.mono(fontSize: 8, color: textSecondary),
        ),
      ],
    );
  }

Widget _buildMusicRingPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Stack(children: [
      Positioned.fill(child: CustomPaint(painter: _MiniDotGridPainter(textSecondary.withValues(alpha: 0.3)))),
      Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Container(
          width: 56, height: 56,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: NothingTheme.accent, width: 2),
          ),
          child: Center(child: Icon(Icons.music_note_outlined, size: 24, color: textPrimary)),
        ),
        const SizedBox(height: 6),
        Text('NOW PLAYING', style: NothingTheme.mono(fontSize: 8, color: textSecondary)),
        const SizedBox(height: 2),
        Text('TRACK', style: NothingTheme.grotesk(fontSize: 11, color: textPrimary)),
        const SizedBox(height: 6),
        Container(height: 2, width: 48,
          decoration: BoxDecoration(borderRadius: BorderRadius.circular(1),
            gradient: LinearGradient(colors: [NothingTheme.accent, borderColor])),
        ),
      ]),
    ]);
  }

  Widget _buildMusicVinylPreview(Color textPrimary, Color textSecondary) {
    return Stack(
      children: [
        Positioned.fill(
          child: CustomPaint(
            painter: _MiniDotGridPainter(textSecondary.withValues(alpha: 0.3)),
          ),
        ),
        Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              width: 64,
              height: 64,
              child: Stack(
                alignment: Alignment.center,
                children: [
                  // Outer ring
                  Container(
                    width: 64,
                    height: 64,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(color: textPrimary, width: 2),
                    ),
                  ),
                  // Grooves
                  ...List.generate(3, (i) => Container(
                    width: 44.0 + i * 8,
                    height: 44.0 + i * 8,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(color: textSecondary.withValues(alpha: 0.4), width: 0.5),
                    ),
                  )),
                  // Label
                  Container(
                    width: 24,
                    height: 24,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: textPrimary,
                    ),
                    child: Center(
                      child: Container(
                        width: 6,
                        height: 6,
                        decoration: const BoxDecoration(
                          shape: BoxShape.circle,
                          color: NothingTheme.accent,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'VINYL',
              style: NothingTheme.mono(fontSize: 8, color: textSecondary),
            ),
            const SizedBox(height: 2),
            Text(
              'TAP TO SPIN',
              style: NothingTheme.mono(fontSize: 6, color: textSecondary),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildMusicCompactPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Stack(children: [
      Positioned.fill(child: CustomPaint(painter: _MiniDotGridPainter(textSecondary.withValues(alpha: 0.3)))),
      Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Container(width: 52, height: 52,
          decoration: BoxDecoration(shape: BoxShape.circle, color: textSecondary.withValues(alpha: 0.12), border: Border.all(color: borderColor, width: 1)),
          child: Center(child: Icon(Icons.music_note, size: 20, color: textPrimary))),
        const SizedBox(height: 6),
        Text('COMPACT', style: NothingTheme.mono(fontSize: 7, color: textSecondary)),
        const SizedBox(height: 3),
        Container(height: 2, width: 50,
          decoration: BoxDecoration(borderRadius: BorderRadius.circular(1),
            color: NothingTheme.accent)),
      ]),
    ]);
  }

  Widget _buildMusicTickerPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Stack(children: [
      Positioned.fill(child: CustomPaint(painter: _MiniDotGridPainter(textSecondary.withValues(alpha: 0.3)))),
      Padding(padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
        child: Column(mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Icon(Icons.music_note, size: 12, color: NothingTheme.accent),
            const SizedBox(width: 4),
            Expanded(child: Text('NOW PLAYING', style: NothingTheme.mono(fontSize: 7, color: textSecondary), overflow: TextOverflow.ellipsis)),
          ]),
          const SizedBox(height: 4),
          Text('TRACK NAME', style: NothingTheme.grotesk(fontSize: 10, color: textPrimary), overflow: TextOverflow.ellipsis),
          const SizedBox(height: 6),
          ClipRRect(borderRadius: BorderRadius.circular(1),
            child: LinearProgressIndicator(value: 0.45, backgroundColor: borderColor,
              valueColor: AlwaysStoppedAnimation<Color>(NothingTheme.accent), minHeight: 3)),
        ])),
    ]);
  }

  Widget _buildMusicWavePreview(Color textPrimary, Color textSecondary, Color borderColor) {
    final bars = [0.3, 0.55, 0.75, 0.95, 0.8, 0.65, 0.9, 1.0, 0.88, 0.7, 0.6, 0.82];
    return Stack(children: [
      Positioned.fill(child: CustomPaint(painter: _MiniDotGridPainter(textSecondary.withValues(alpha: 0.3)))),
      Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Text('NOW PLAYING', style: NothingTheme.mono(fontSize: 7, color: textSecondary)),
        const SizedBox(height: 2),
        Text('TRACK', style: NothingTheme.grotesk(fontSize: 11, color: textPrimary)),
        const SizedBox(height: 7),
        SizedBox(height: 28,
          child: Row(mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.end,
            children: bars.map((h) => Container(
              width: 4, height: 28 * h, margin: const EdgeInsets.symmetric(horizontal: 1),
              decoration: BoxDecoration(color: NothingTheme.accent.withValues(alpha: h), borderRadius: BorderRadius.circular(1.5)),
            )).toList())),
        const SizedBox(height: 5),
        Container(height: 2, width: 55,
          decoration: BoxDecoration(borderRadius: BorderRadius.circular(1),
            gradient: LinearGradient(colors: [NothingTheme.accent, borderColor]))),
      ]),
    ]);
  }

  Widget _buildCalculatorPreview(Color textPrimary, Color textSecondary, Color borderColor) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        Text(
          '1,234',
          style: NothingTheme.doto(fontSize: 32, color: textPrimary),
        ),
        const SizedBox(height: 8),
        Text(
          '739 + 495',
          style: NothingTheme.mono(fontSize: 10, color: textSecondary),
        ),
      ],
    );
  }
}

class _AnalogClockPainter extends CustomPainter {
  final Color color;

  _AnalogClockPainter(this.color);

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;
    
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    for (int i = 0; i < 12; i++) {
      final angle = (i * 30 - 90) * math.pi / 180;
      final markerRadius = i % 3 == 0 ? 2.5 : 1.5;
      final mx = center.dx + (radius - 8) * math.cos(angle);
      final my = center.dy + (radius - 8) * math.sin(angle);
      canvas.drawCircle(Offset(mx, my), markerRadius, paint);
    }

    final hour = 5;
    final minute = 30;
    final hourAngle = ((hour % 12 + minute / 60) * 30 - 90) * math.pi / 180;
    final minuteAngle = (minute * 6 - 90) * math.pi / 180;

    paint.style = PaintingStyle.stroke;
    paint.strokeCap = StrokeCap.round;

    paint.strokeWidth = 3.5;
    canvas.drawLine(center, Offset(center.dx + radius * 0.45 * math.cos(hourAngle), center.dy + radius * 0.45 * math.sin(hourAngle)), paint);

    paint.strokeWidth = 2.0;
    canvas.drawLine(center, Offset(center.dx + radius * 0.65 * math.cos(minuteAngle), center.dy + radius * 0.65 * math.sin(minuteAngle)), paint);

    paint.style = PaintingStyle.fill;
    canvas.drawCircle(center, 4, paint);
    
    paint.color = const Color(0xFFD71921); // Red accent
    canvas.drawCircle(center, 1.5, paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _MiniDotGridPainter extends CustomPainter {
  final Color color;

  _MiniDotGridPainter(this.color);

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color.withAlpha(40)
      ..style = PaintingStyle.fill;
    const spacing = 12.0;
    for (double x = spacing; x < size.width; x += spacing) {
      for (double y = spacing; y < size.height; y += spacing) {
        canvas.drawCircle(Offset(x, y), 0.8, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class DinoPixelPainter extends CustomPainter {
  final Color color;

  DinoPixelPainter(this.color);

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;
    canvas.drawRect(Rect.fromLTWH(size.width * 0.4, 0, size.width * 0.6, size.height * 0.4), paint);
    paint.color = color.computeLuminance() > 0.5 ? const Color(0xFF000000) : const Color(0xFFFFFFFF);
    canvas.drawRect(Rect.fromLTWH(size.width * 0.55, size.height * 0.1, size.width * 0.1, size.height * 0.1), paint);
    paint.color = color;
    canvas.drawRect(Rect.fromLTWH(size.width * 0.6, size.height * 0.25, size.width * 0.4, size.height * 0.05), paint);
    canvas.drawRect(Rect.fromLTWH(size.width * 0.2, size.height * 0.35, size.width * 0.4, size.height * 0.4), paint);
    canvas.drawRect(Rect.fromLTWH(0, size.height * 0.3, size.width * 0.2, size.height * 0.1), paint);
    canvas.drawRect(Rect.fromLTWH(size.width * 0.25, size.height * 0.75, size.width * 0.1, size.height * 0.25), paint);
    canvas.drawRect(Rect.fromLTWH(size.width * 0.45, size.height * 0.75, size.width * 0.1, size.height * 0.25), paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _DotMatrixPainter extends CustomPainter {
  final String text;
  final Color color;

  static const _patterns = {
    '0': [
      " ███ ",
      "█   █",
      "█   █",
      "█   █",
      "█   █",
      "█   █",
      " ███ ",
    ],
    '1': [
      "  █  ",
      " ██  ",
      "  █  ",
      "  █  ",
      "  █  ",
      "  █  ",
      " ███ ",
    ],
    '2': [
      " ███ ",
      "█   █",
      "    █",
      "   █ ",
      "  █  ",
      " █   ",
      "█████",
    ],
    '3': [
      " ███ ",
      "█   █",
      "    █",
      "  ██ ",
      "    █",
      "█   █",
      " ███ ",
    ],
    '4': [
      "   █ ",
      "  ██ ",
      " █ █ ",
      "█  █ ",
      "█████",
      "   █ ",
      "   █ ",
    ],
    '5': [
      "█████",
      "█    ",
      "████ ",
      "    █",
      "    █",
      "█   █",
      " ███ ",
    ],
    '6': [
      " ███ ",
      "█   █",
      "█    ",
      "████ ",
      "█   █",
      "█   █",
      " ███ ",
    ],
    '7': [
      "█████",
      "    █",
      "   █ ",
      "  █  ",
      " █   ",
      " █   ",
      " █   ",
    ],
    '8': [
      " ███ ",
      "█   █",
      "█   █",
      " ███ ",
      "█   █",
      "█   █",
      " ███ ",
    ],
    '9': [
      " ███ ",
      "█   █",
      "█   █",
      " ████",
      "    █",
      "█   █",
      " ███ ",
    ],
    ':': [
      "     ",
      "  █  ",
      "  █  ",
      "     ",
      "  █  ",
      "  █  ",
      "     ",
    ],
  };

  _DotMatrixPainter({required this.text, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;
    final dotSize = size.height / 14;
    final dotSpacing = dotSize * 0.4;
    final charWidth = 5 * (dotSize + dotSpacing);
    final charGap = dotSpacing * 2;
    final totalWidth = text.length * (charWidth + charGap) - charGap;
    var currentX = (size.width - totalWidth) / 2;

    for (var char in text.runes.map((r) => String.fromCharCode(r))) {
      final pattern = _patterns[char] ?? _patterns['0']!;
      for (var row = 0; row < pattern.length; row++) {
        for (var col = 0; col < pattern[row].length; col++) {
          if (pattern[row][col] == '█') {
            final dotX = currentX + col * (dotSize + dotSpacing);
            final dotY = row * (dotSize + dotSpacing);
            canvas.drawCircle(Offset(dotX, dotY), dotSize / 2, paint);
          }
        }
      }
      currentX += charWidth + charGap;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

class _LiveClockPreview extends StatelessWidget {
  final Color textPrimary;
  final Color textSecondary;
  final bool is24Hour;

  const _LiveClockPreview({required this.textPrimary, required this.textSecondary, required this.is24Hour});

  @override
  Widget build(BuildContext context) {
    final now = DateTime(2026, 5, 15, 17, 30, 0);
    final hour = is24Hour ? now.hour : (now.hour % 12 == 0 ? 12 : now.hour % 12);
    final minute = now.minute.toString().padLeft(2, '0');
    final period = is24Hour ? '' : (now.hour >= 12 ? 'PM' : 'AM');
    final dotMatrixTime = '${hour.toString().padLeft(2, '0')}:$minute';

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'TIME',
              style: NothingTheme.mono(fontSize: 7, color: textSecondary),
            ),
            if (period.isNotEmpty)
              Text(
                period,
                style: NothingTheme.mono(fontSize: 7, color: NothingTheme.accent),
              ),
          ],
        ),
        Center(
          child: SizedBox(
            height: 42,
            child: CustomPaint(
              painter: _DotMatrixPainter(
                text: dotMatrixTime,
                color: textPrimary,
              ),
            ),
          ),
        ),
        Text(
          'FRIDAY, MAY 15',
          style: NothingTheme.mono(fontSize: 7, color: textPrimary),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
      ],
    );
  }
}

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'core/theme.dart';
import 'widget_selection_screen.dart';
import 'onboarding_screen.dart';

void main() {
  runApp(const DotOSApp());
}

class DotOSApp extends StatefulWidget {
  const DotOSApp({super.key});

  @override
  State<DotOSApp> createState() => _DotOSAppState();
}

class _DotOSAppState extends State<DotOSApp> {
  ThemeMode _themeMode = ThemeMode.dark;

  void _toggleTheme() {
    setState(() {
      _themeMode = _themeMode == ThemeMode.dark
          ? ThemeMode.light
          : ThemeMode.dark;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DotOS',
      debugShowCheckedModeBanner: false,
      theme: NothingTheme.lightTheme(),
      darkTheme: NothingTheme.darkTheme(),
      themeMode: _themeMode,
      home: SplashScreen(onToggleTheme: _toggleTheme),
      routes: {
        '/home': (context) => DashboardScreen(onToggleTheme: _toggleTheme),
        '/onboarding': (context) => const OnboardingScreen(),
      },
    );
  }
}

class SplashScreen extends StatefulWidget {
  final VoidCallback onToggleTheme;

  const SplashScreen({super.key, required this.onToggleTheme});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _checkOnboarding();
  }

  Future<void> _checkOnboarding() async {
    await Future.delayed(const Duration(milliseconds: 500));
    final prefs = await SharedPreferences.getInstance();
    final onboardingComplete = prefs.getBool('onboarding_complete') ?? false;

    if (mounted) {
      if (onboardingComplete) {
        Navigator.of(context).pushReplacementNamed('/home');
      } else {
        Navigator.of(context).pushReplacementNamed('/onboarding');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = isDark ? const Color(0xFF000000) : const Color(0xFFF5F5F0);
    final textColor = isDark
        ? const Color(0xFFFFFFFF)
        : const Color(0xFF000000);

    return Scaffold(
      backgroundColor: bgColor,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'DOTOS',
              style: NothingTheme.doto(fontSize: 72, color: textColor),
            ),
            const SizedBox(height: 32),
            CircularProgressIndicator(
              color: isDark ? const Color(0xFF999999) : const Color(0xFF666666),
              strokeWidth: 2,
            ),
          ],
        ),
      ),
    );
  }
}

class DashboardScreen extends StatefulWidget {
  final VoidCallback onToggleTheme;

  const DashboardScreen({super.key, required this.onToggleTheme});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  static const List<_WidgetCategory> _categories = [
    _WidgetCategory(
      'CLOCK',
      '3 STYLES',
      'clock',
      'time digital analog binary dot matrix',
    ),
    _WidgetCategory(
      'CALENDAR',
      '5 STYLES',
      'calendar',
      'date day week events month agenda',
    ),
    _WidgetCategory(
      'BATTERY',
      '5 STYLES',
      'battery',
      'percentage circular bars minimal detailed charging power',
    ),
    _WidgetCategory(
      'STORAGE',
      '5 STYLES',
      'storage',
      'usage ring compact analysis space disk',
    ),
    _WidgetCategory(
      'SCREEN TIME',
      '3 STYLES',
      'screen_time',
      'usage stats daily screen time timer ring dashboard today',
    ),
    _WidgetCategory(
      'CALCULATOR',
      '1 STYLE',
      'calculator',
      'math standard numbers',
    ),
    _WidgetCategory(
      'SOUND',
      '3 STYLES',
      'sound',
      'sound ring vibrate silent dnd volume',
    ),
    _WidgetCategory(
      'TAP COUNTER',
      '3 STYLES',
      'utilities',
      'tap counter classic dial matrix utility',
    ),
    _WidgetCategory(
      'GAMES',
      '4 STYLES',
      'games',
      'spinner bottle spin dice dino game runner play fun',
    ),
    _WidgetCategory(
      'MUSIC',
      '5 STYLES',
      'music',
      'songs play media vinyl audio player tracks ring compact ticker wave',
    ),
  ];

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = Theme.of(context).scaffoldBackgroundColor;
    final textPrimary = Theme.of(context).colorScheme.onSurface;
    final textSecondary = NothingTheme.textSecondary;
    final textDisabled = NothingTheme.textDisabled;
    final borderColor = Theme.of(context).colorScheme.outline;
    final surfaceColor = Theme.of(context).colorScheme.surface;
    final surfaceRaised = isDark
        ? NothingTheme.surfaceRaised
        : const Color(0xFFF0F0F0);
    final innerSurface = isDark ? NothingTheme.black : const Color(0xFFFFFFFF);
    final query = _searchQuery.trim().toLowerCase();
    final visibleCategories = query.isEmpty
        ? _categories
        : _categories.where((category) {
            return category.title.toLowerCase().contains(query) ||
                category.subtitle.toLowerCase().contains(query) ||
                category.category.toLowerCase().contains(query) ||
                category.keywords.toLowerCase().contains(query);
          }).toList();

    return Scaffold(
      backgroundColor: bgColor,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 28, 24, 16),
              child: Row(
                children: [
                  Expanded(
                    child: FittedBox(
                      alignment: Alignment.centerLeft,
                      fit: BoxFit.scaleDown,
                      child: Text(
                        'DOTOS',
                        style: NothingTheme.doto(
                          fontSize: 28,
                          color: textPrimary,
                        ),
                        maxLines: 1,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  GestureDetector(
                    onTap: widget.onToggleTheme,
                    child: Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        border: Border.all(color: borderColor, width: 1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Icon(
                        isDark
                            ? Icons.light_mode_outlined
                            : Icons.dark_mode_outlined,
                        color: textSecondary,
                        size: 18,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
              child: _buildSearchField(
                textPrimary: textPrimary,
                textSecondary: textSecondary,
                borderColor: borderColor,
                surfaceColor: surfaceColor,
                onChanged: (value) {
                  setState(() {
                    _searchQuery = value;
                  });
                },
                onClear: _searchQuery.isEmpty
                    ? null
                    : () {
                        setState(() {
                          _searchQuery = '';
                          _searchController.clear();
                        });
                      },
              ),
            ),
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
                children: visibleCategories.isEmpty
                    ? [
                        Padding(
                          padding: const EdgeInsets.only(top: 48),
                          child: Center(
                            child: Text(
                              'NO WIDGETS FOUND',
                              style: NothingTheme.mono(
                                fontSize: 12,
                                color: textSecondary,
                              ),
                            ),
                          ),
                        ),
                      ]
                    : visibleCategories
                          .map(
                            (category) => _buildCategoryCard(
                              context,
                              category.title,
                              category.subtitle,
                              category.category,
                              textPrimary,
                              textSecondary,
                              textDisabled,
                              borderColor,
                              surfaceRaised,
                              innerSurface,
                            ),
                          )
                          .toList(),
              ),
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }

  Widget _buildCategoryCard(
    BuildContext context,
    String title,
    String subtitle,
    String category,
    Color textPrimary,
    Color textSecondary,
    Color textDisabled,
    Color borderColor,
    Color surfaceRaised,
    Color innerSurface,
  ) {
    return GestureDetector(
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => WidgetSelectionScreen(
            category: category,
            title: title,
            onToggleTheme: widget.onToggleTheme,
          ),
        ),
      ),
      child: Container(
        height: 160,
        margin: const EdgeInsets.only(bottom: 20),
        decoration: BoxDecoration(
          color: surfaceRaised,
          border: Border.all(color: borderColor, width: 1),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      subtitle,
                      style: NothingTheme.mono(
                        fontSize: 10,
                        color: textDisabled,
                      ),
                    ),
                    const Spacer(),
                    Text(
                      title,
                      style: NothingTheme.mono(
                        fontSize: 12,
                        color: textPrimary,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 16),
              Container(
                width: 170,
                height: 120,
                decoration: BoxDecoration(
                  color: innerSurface,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: borderColor, width: 1),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Center(
                    child: _buildCategoryPreview(
                      category,
                      textPrimary,
                      textSecondary,
                      borderColor,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCategoryPreview(
    String category,
    Color textPrimary,
    Color textSecondary,
    Color borderColor,
  ) {
    switch (category) {
      case 'clock':
        return Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            FittedBox(
              fit: BoxFit.scaleDown,
              child: Text(
                '17:30',
                style: NothingTheme.doto(fontSize: 36, color: textPrimary),
              ),
            ),
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(5, (i) => Container(
                width: 5,
                height: 5,
                margin: const EdgeInsets.symmetric(horizontal: 3),
                decoration: BoxDecoration(
                  color: i == 2 ? NothingTheme.accent : textSecondary,
                  shape: BoxShape.circle,
                ),
              )),
            ),
          ],
        );
      case 'calendar':
        return Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                border: Border.all(color: textSecondary, width: 1),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Center(
                child: Text(
                  '30',
                  style: NothingTheme.doto(fontSize: 22, color: textPrimary),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  'APRIL',
                  style: NothingTheme.mono(fontSize: 10, color: textPrimary),
                ),
                const SizedBox(height: 4),
                Text(
                  '2026',
                  style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                ),
              ],
            ),
          ],
        );
      case 'battery':
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '85',
                  style: NothingTheme.doto(fontSize: 30, color: textPrimary),
                ),
                const SizedBox(width: 4),
                Text(
                  '%',
                  style: NothingTheme.mono(fontSize: 12, color: textSecondary),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: List.generate(8, (index) {
                return Container(
                  width: 10,
                  height: 6,
                  margin: const EdgeInsets.only(right: 4),
                  decoration: BoxDecoration(
                    color: index < 6 ? textPrimary : borderColor,
                    borderRadius: BorderRadius.circular(2),
                  ),
                );
              }),
            ),
          ],
        );
      case 'storage':
        return Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '76.8 GB',
              style: NothingTheme.doto(fontSize: 20, color: textPrimary),
            ),
            const SizedBox(height: 8),
            ClipRRect(
              borderRadius: BorderRadius.circular(2),
              child: LinearProgressIndicator(
                value: 0.6,
                backgroundColor: borderColor,
                valueColor: AlwaysStoppedAnimation(textPrimary),
                minHeight: 4,
              ),
            ),
          ],
        );
      case 'screen_time':
        return Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '3h 42m',
              style: NothingTheme.doto(fontSize: 24, color: textPrimary),
            ),
            const SizedBox(height: 8),
            Text(
              'SCREEN TIME',
              style: NothingTheme.mono(fontSize: 9, color: textSecondary),
            ),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(2),
              child: LinearProgressIndicator(
                value: 0.46,
                backgroundColor: borderColor,
                valueColor: AlwaysStoppedAnimation(textPrimary),
                minHeight: 4,
              ),
            ),
          ],
        );
      case 'calculator':
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'CALC',
              style: NothingTheme.mono(fontSize: 9, color: textSecondary),
            ),
            const SizedBox(height: 6),
            Text(
              '19',
              style: NothingTheme.doto(fontSize: 28, color: textPrimary),
            ),
            const SizedBox(height: 4),
            Text(
              '12 + 7',
              style: NothingTheme.mono(fontSize: 9, color: textSecondary),
            ),
          ],
        );
      case 'sound':
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'SOUND',
              style: NothingTheme.mono(fontSize: 9, color: textSecondary),
            ),
            const SizedBox(height: 6),
            Text(
              'RING',
              style: NothingTheme.doto(fontSize: 26, color: textPrimary),
            ),
            const SizedBox(height: 4),
            Text(
              'TAP TO TOGGLE',
              style: NothingTheme.mono(fontSize: 8, color: textSecondary),
            ),
          ],
        );
      case 'utilities':
        return Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 50,
              height: 50,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: textPrimary, width: 2),
              ),
              child: Center(
                child: Text(
                  '12',
                  style: NothingTheme.doto(fontSize: 18, color: textPrimary),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  'TAPS',
                  style: NothingTheme.mono(fontSize: 10, color: textPrimary),
                ),
                Text(
                  'TOTAL',
                  style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                ),
              ],
            ),
          ],
        );
      case 'games':
        return Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                border: Border.all(color: textPrimary, width: 1.5),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Center(
                child: Text(
                  '🎯',
                  style: TextStyle(fontSize: 0),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  'GAMES',
                  style: NothingTheme.mono(fontSize: 10, color: textPrimary),
                ),
                Text(
                  'FUN',
                  style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                ),
              ],
            ),
          ],
        );
      case 'music':
        return Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: textPrimary, width: 1.5),
              ),
              child: Center(
                child: Icon(Icons.music_note_outlined, size: 20, color: textPrimary),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'NIGHTS',
                    style: NothingTheme.grotesk(fontSize: 16, fontWeight: FontWeight.w500, color: textPrimary),
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 2),
                  Text(
                    'FRANK OCEAN',
                    style: NothingTheme.mono(fontSize: 8, color: textSecondary),
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
          ],
        );
      default:
        return const SizedBox();
    }
  }

  Widget _buildSearchField({
    required Color textPrimary,
    required Color textSecondary,
    required Color borderColor,
    required Color surfaceColor,
    required ValueChanged<String> onChanged,
    VoidCallback? onClear,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: surfaceColor,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: borderColor, width: 1),
      ),
      child: TextField(
        controller: _searchController,
        cursorColor: textPrimary,
        onChanged: onChanged,
        style: NothingTheme.grotesk(fontSize: 16, color: textPrimary),
        decoration: InputDecoration(
          hintText: 'Search widgets',
          hintStyle: NothingTheme.grotesk(fontSize: 16, color: textSecondary),
          prefixIcon: Icon(Icons.search, color: textSecondary),
          suffixIcon: onClear == null
              ? null
              : IconButton(
                  onPressed: onClear,
                  icon: Icon(Icons.close, color: textSecondary, size: 18),
                ),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 20,
            vertical: 14,
          ),
        ),
      ),
    );
  }
}

class _WidgetCategory {
  final String title;
  final String subtitle;
  final String category;
  final String keywords;

  const _WidgetCategory(
    this.title,
    this.subtitle,
    this.category,
    this.keywords,
  );
}

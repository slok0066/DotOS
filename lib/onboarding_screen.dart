import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'core/theme.dart';
import 'core/components.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> with WidgetsBindingObserver {
  static const platform = MethodChannel('permission_channel');
  int _currentPage = 0;
  final PageController _pageController = PageController();
  
  // Permission status tracking
  Map<String, bool> permissionStatus = {
    'battery': false,
    'storage': false,
    'calendar': false,
    'notification': false,
    'media': false,
    'notification_listener': false,
    'sound': false,
    'usage_stats': false,
  };
  
  final List<OnboardingPage> _pages = [
    OnboardingPage(
      title: 'WELCOME TO DOTOS',
      description: 'A Nothing-inspired widget system for your Android home screen. Minimal, functional, beautiful.',
      icon: Icons.widgets_outlined,
      permissionType: null,
    ),
    OnboardingPage(
      title: 'BATTERY ACCESS',
      description: 'Allow DotOS to read battery status for real-time battery widgets with accurate percentage and charging state.',
      icon: Icons.battery_charging_full,
      permissionType: 'battery',
    ),
    OnboardingPage(
      title: 'STORAGE ACCESS',
      description: 'Allow DotOS to read storage information to display accurate storage usage in widgets.',
      icon: Icons.storage,
      permissionType: 'storage',
    ),
    OnboardingPage(
      title: 'CALENDAR ACCESS',
      description: 'Allow DotOS to read your calendar events to display upcoming events in the calendar widget.',
      icon: Icons.calendar_today,
      permissionType: 'calendar',
    ),
    OnboardingPage(
      title: 'NOTIFICATION ACCESS',
      description: 'Allow DotOS to show notifications for widget updates and important information.',
      icon: Icons.notifications_outlined,
      permissionType: 'notification',
    ),
    OnboardingPage(
      title: 'MEDIA ACCESS',
      description: 'Allow DotOS to read media info for the Music Widget to display now-playing tracks, album art, and control playback.',
      icon: Icons.headphones_outlined,
      permissionType: 'media',
    ),
    OnboardingPage(
      title: 'MEDIA PLAYER ACCESS',
      description: 'Grant Notification Listener access so the Music Widget can read the currently playing song, artist, and playback progress from any music app.',
      icon: Icons.music_note_outlined,
      permissionType: 'notification_listener',
    ),
    OnboardingPage(
      title: 'SOUND CONTROL',
      description: 'Allow DotOS to toggle ring, vibrate, and silent modes from the sound widget.',
      icon: Icons.volume_up_outlined,
      permissionType: 'sound',
    ),
    OnboardingPage(
      title: 'USAGE ACCESS',
      description: 'Allow DotOS to read daily app usage so Screen Time widgets can display real device stats.',
      icon: Icons.query_stats_outlined,
      permissionType: 'usage_stats',
    ),
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkAllPermissions();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _pageController.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkAllPermissions();
    }
  }

  Future<void> _checkAllPermissions() async {
    for (var type in ['battery', 'storage', 'calendar', 'notification', 'media', 'notification_listener', 'sound', 'usage_stats']) {
      await _checkPermissionStatus(type);
    }
  }

  Future<void> _checkPermissionStatus(String permissionType) async {
    try {
      final bool granted = await platform.invokeMethod('checkPermission', {
        'type': permissionType,
      });
      
      if (mounted) {
        setState(() {
          permissionStatus[permissionType] = granted;
        });
      }
    } catch (e) {
      // Permission check failed, assume not granted
      if (mounted) {
        setState(() {
          permissionStatus[permissionType] = false;
        });
      }
    }
  }

  Future<void> _requestPermission(String permissionType) async {
    HapticFeedback.mediumImpact();
    try {
      final bool granted = await platform.invokeMethod('requestPermission', {
        'type': permissionType,
      });
      
      if (mounted) {
        setState(() {
          permissionStatus[permissionType] = granted;
        });
        
        final isSettingsNav = permissionType == 'sound' || permissionType == 'usage_stats';
        if (granted) {
          HapticFeedback.heavyImpact();
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                'PERMISSION GRANTED ✓',
                style: NothingTheme.mono(fontSize: 12, color: NothingTheme.black),
              ),
              backgroundColor: NothingTheme.success,
              duration: const Duration(seconds: 2),
            ),
          );
        } else if (!isSettingsNav) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                'PERMISSION DENIED',
                style: NothingTheme.mono(fontSize: 12, color: NothingTheme.black),
              ),
              backgroundColor: NothingTheme.accent,
              duration: const Duration(seconds: 2),
            ),
          );
        }
      }
    } on PlatformException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'ERROR: ${e.message}',
              style: NothingTheme.mono(fontSize: 12, color: NothingTheme.black),
            ),
            backgroundColor: NothingTheme.accent,
          ),
        );
      }
    }
  }

  Future<void> _completeOnboarding() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('onboarding_complete', true);
    if (mounted) {
      Navigator.of(context).pushReplacementNamed('/home');
    }
  }

  void _nextPage() {
    HapticFeedback.lightImpact();
    if (_currentPage < _pages.length - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    } else {
      _completeOnboarding();
    }
  }

  void _skipOnboarding() {
    HapticFeedback.lightImpact();
    _completeOnboarding();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: DotGridBackground(
        child: SafeArea(
          child: Column(
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(NothingTheme.spaceLG),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'DOTOS',
                      style: NothingTheme.mono(
                        fontSize: 14,
                        color: NothingTheme.textPrimary,
                      ),
                    ),
                    if (_currentPage < _pages.length - 1)
                      TextButton(
                        onPressed: _skipOnboarding,
                        child: Text(
                          'SKIP',
                          style: NothingTheme.mono(
                            fontSize: 12,
                            color: NothingTheme.textSecondary,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
              
              // Page content
              Expanded(
                child: PageView.builder(
                  controller: _pageController,
                  onPageChanged: (index) {
                    setState(() {
                      _currentPage = index;
                    });
                  },
                  itemCount: _pages.length,
                  itemBuilder: (context, index) {
                    return _buildPage(_pages[index]);
                  },
                ),
              ),
              
              // Page indicators
              Padding(
                padding: const EdgeInsets.symmetric(vertical: NothingTheme.spaceLG),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(
                    _pages.length,
                    (index) => Container(
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      width: _currentPage == index ? 24 : 8,
                      height: 8,
                      decoration: BoxDecoration(
                        color: _currentPage == index
                            ? NothingTheme.textDisplay
                            : NothingTheme.border,
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                  ),
                ),
              ),
              
              // Action buttons
              Padding(
                padding: const EdgeInsets.all(NothingTheme.spaceLG),
                child: Column(
                  children: [
                    // Empty permission state warning
                    if (_currentPage > 0 && _pages.sublist(1, _currentPage + 1).every(
                          (page) => page.permissionType == null || permissionStatus[page.permissionType] == false,
                        ))
                      Container(
                        margin: const EdgeInsets.only(bottom: NothingTheme.spaceMD),
                        padding: const EdgeInsets.all(NothingTheme.spaceMD),
                        decoration: BoxDecoration(
                          color: NothingTheme.warning.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: NothingTheme.warning, width: 1),
                        ),
                        child: Row(
                          children: [
                            Icon(Icons.warning_amber_outlined, size: 20, color: NothingTheme.warning),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                'Some features may not work without permissions. You can continue and grant them later in settings.',
                                style: NothingTheme.mono(fontSize: 10, color: NothingTheme.warning),
                              ),
                            ),
                          ],
                        ),
                      ),
                    if (_pages[_currentPage].permissionType != null)
                      Padding(
                        padding: const EdgeInsets.only(bottom: NothingTheme.spaceMD),
                        child: NothingButton(
                          label: permissionStatus[_pages[_currentPage].permissionType] == true
                              ? 'Permission Granted ✓'
                              : 'Grant Permission',
                          onTap: permissionStatus[_pages[_currentPage].permissionType] == true
                              ? null
                              : () => _requestPermission(_pages[_currentPage].permissionType!),
                          primary: permissionStatus[_pages[_currentPage].permissionType] != true,
                        ),
                      ),
                    NothingButton(
                      label: _currentPage == _pages.length - 1 ? 'Get Started' : 'Next',
                      onTap: _nextPage,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPage(OnboardingPage page) {
    final isGranted = page.permissionType != null && 
                      permissionStatus[page.permissionType] == true;
    
    return Padding(
      padding: const EdgeInsets.all(NothingTheme.space2XL),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Icon with status indicator
          Stack(
            children: [
              Container(
                width: 120,
                height: 120,
                decoration: BoxDecoration(
                  border: Border.all(
                    color: isGranted ? NothingTheme.success : NothingTheme.borderVisible,
                    width: 2,
                  ),
                  borderRadius: BorderRadius.circular(60),
                ),
                child: Icon(
                  page.icon,
                  size: 64,
                  color: isGranted ? NothingTheme.success : NothingTheme.textPrimary,
                ),
              ),
              if (isGranted)
                Positioned(
                  right: 0,
                  bottom: 0,
                  child: Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: NothingTheme.success,
                      shape: BoxShape.circle,
                      border: Border.all(color: NothingTheme.black, width: 2),
                    ),
                    child: const Icon(
                      Icons.check,
                      color: NothingTheme.black,
                      size: 20,
                    ),
                  ),
                ),
            ],
          ),
          
          const SizedBox(height: NothingTheme.space3XL),
          
          // Title
          Text(
            page.title,
            style: NothingTheme.mono(
              fontSize: 18,
              color: NothingTheme.textDisplay,
            ),
            textAlign: TextAlign.center,
          ),
          
          const SizedBox(height: NothingTheme.spaceLG),
          
          // Description
          Text(
            page.description,
            style: NothingTheme.mono(
              fontSize: 12,
              color: NothingTheme.textSecondary,
            ),
            textAlign: TextAlign.center,
          ),
          
          // Permission status
          if (page.permissionType != null)
            Padding(
              padding: const EdgeInsets.only(top: NothingTheme.spaceLG),
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: NothingTheme.spaceMD,
                  vertical: NothingTheme.spaceSM,
                ),
                decoration: BoxDecoration(
                  color: isGranted 
                      ? NothingTheme.success.withValues(alpha: 0.1)
                      : NothingTheme.surfaceRaised,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: isGranted ? NothingTheme.success : NothingTheme.borderVisible,
                  ),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      isGranted ? Icons.check_circle : Icons.info_outline,
                      size: 16,
                      color: isGranted ? NothingTheme.success : NothingTheme.textSecondary,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isGranted ? 'GRANTED' : 'NOT GRANTED',
                      style: NothingTheme.mono(
                        fontSize: 11,
                        color: isGranted ? NothingTheme.success : NothingTheme.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class OnboardingPage {
  final String title;
  final String description;
  final IconData icon;
  final String? permissionType;

  OnboardingPage({
    required this.title,
    required this.description,
    required this.icon,
    this.permissionType,
  });
}
